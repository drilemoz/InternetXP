package br.com.internetxp;

import android.content.Context;
import android.net.Uri;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/** Arquivos locais de extensões: nunca extrai código e nunca confunde pacote XPI com extensão instalada. */
final class ExtensionPackageStore {
    static final long MAX_PACKAGE_BYTES = 40L * 1024 * 1024;
    private static final long MAX_MANIFEST_BYTES = 1024 * 1024;
    private final Context context;

    static final class Inspection {
        final String name;
        final String version;
        final int manifestVersion;
        final String warning;
        Inspection(String name, String version, int manifestVersion, String warning) {
            this.name = name; this.version = version;
            this.manifestVersion = manifestVersion; this.warning = warning;
        }
    }

    ExtensionPackageStore(Context context) { this.context = context.getApplicationContext(); }

    File copyToStage(Uri source) throws IOException {
        if (source == null || (!"content".equals(source.getScheme()) && !"file".equals(source.getScheme())))
            throw new IOException("Selecione um arquivo XPI local válido.");
        File directory = new File(context.getCacheDir(), "xpi-stage");
        if (!directory.exists() && !directory.mkdirs()) throw new IOException("Não foi possível preparar o armazenamento.");
        File target = File.createTempFile("extension-", ".xpi", directory);
        try (InputStream in = context.getContentResolver().openInputStream(source);
             OutputStream out = new FileOutputStream(target)) {
            if (in == null) throw new IOException("O Android não permitiu ler o arquivo.");
            byte[] buffer = new byte[32768];
            long total = 0;
            int n;
            while ((n = in.read(buffer)) != -1) {
                total += n;
                if (total > MAX_PACKAGE_BYTES) throw new IOException("XPI maior que o limite de 40 MiB.");
                out.write(buffer, 0, n);
            }
            if (total == 0) throw new IOException("Arquivo vazio.");
            return target;
        } catch (Exception error) {
            target.delete();
            if (error instanceof IOException) throw (IOException) error;
            throw new IOException("Falha ao importar XPI: " + error.getMessage(), error);
        }
    }

    static Inspection inspect(File xpi) throws IOException {
        if (!xpi.isFile() || xpi.length() == 0 || xpi.length() > MAX_PACKAGE_BYTES)
            throw new IOException("Arquivo XPI vazio ou grande demais.");
        try (ZipFile zip = new ZipFile(xpi)) {
            ZipEntry manifest = zip.getEntry("manifest.json");
            if (manifest == null || manifest.isDirectory()) throw new IOException("Pacote sem manifest.json na raiz.");
            if (manifest.getSize() > MAX_MANIFEST_BYTES)
                throw new IOException("Manifesto acima do limite de segurança.");
            byte[] data;
            try (InputStream in = zip.getInputStream(manifest);
                 java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream()) {
                byte[] buffer = new byte[8192];
                int n;
                while ((n = in.read(buffer)) != -1) {
                    if (out.size() + n > MAX_MANIFEST_BYTES) throw new IOException("Manifesto muito grande.");
                    out.write(buffer, 0, n);
                }
                data = out.toByteArray();
            }
            JSONObject json = new JSONObject(new String(data, StandardCharsets.UTF_8));
            int mv = json.optInt("manifest_version", -1);
            String name = json.optString("name", "").trim();
            String version = json.optString("version", "").trim();
            if (mv != 2 && mv != 3) throw new IOException("Versão de manifesto não suportada: " + mv);
            if (name.isEmpty() || version.isEmpty()) throw new IOException("manifest.json sem nome ou versão.");
            String warning = "Análise inicial aprovada. A assinatura e as APIs são verificadas pelo GeckoView na instalação.";
            JSONObject bss = json.optJSONObject("browser_specific_settings");
            if (bss == null) bss = json.optJSONObject("applications");
            JSONObject gecko = bss == null ? null : bss.optJSONObject("gecko");
            if (gecko != null) {
                String minimum = gecko.optString("strict_min_version", "");
                if (!minimum.isEmpty() && ExtensionCompatibility.requiresNewerFirefox(minimum, 158))
                    throw new IOException("Esta extensão exige Firefox " + minimum + "; o motor atual é GeckoView 158.");
                String maximum = gecko.optString("strict_max_version", "");
                if (!maximum.isEmpty() && ExtensionCompatibility.isBelowMinimumFirefox(maximum, 158))
                    throw new IOException("A extensão limita o Firefox à versão " + maximum + "; o motor atual é GeckoView 158.");
            }
            if (mv == 3) warning += " Manifest V3: algumas APIs móveis podem ser limitadas.";
            return new Inspection(name, version, mv, warning);
        } catch (org.json.JSONException ex) {
            throw new IOException("manifest.json inválido: " + ex.getMessage(), ex);
        }
    }

    private File archiveFile(String id) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(id.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (int i = 0; i < 16; i++) hex.append(String.format(java.util.Locale.ROOT, "%02x", bytes[i] & 255));
            return new File(new File(context.getFilesDir(), "extension-archives"), hex + ".xpi");
        } catch (Exception e) { throw new IllegalStateException("Hash de arquivo indisponível", e); }
    }

    boolean hasArchive(String id) { return archiveFile(id).isFile(); }
    File getArchive(String id) { File file = archiveFile(id); return file.isFile() ? file : null; }
    void removeArchive(String id) { archiveFile(id).delete(); }

    /** O pacote só é preservado após o GeckoView confirmar instalação. */
    void archive(String id, File staged) throws IOException {
        File target = archiveFile(id);
        File parent = target.getParentFile();
        if (!parent.exists() && !parent.mkdirs()) throw new IOException("Sem acesso à pasta de extensões.");
        File temporary = File.createTempFile("copy-", ".part", parent);
        try {
            try (InputStream in = new FileInputStream(staged); OutputStream out = new FileOutputStream(temporary)) {
                byte[] buffer = new byte[32768]; int read;
                while ((read = in.read(buffer)) != -1) out.write(buffer, 0, read);
            }
            if (!temporary.renameTo(target)) {
                // Alguns sistemas não substituem o arquivo original por renameTo.
                if (!target.delete() || !temporary.renameTo(target))
                    throw new IOException("Não foi possível guardar o XPI para exportação.");
            }
        } finally { temporary.delete(); }
    }

    static void copyTo(File from, OutputStream to) throws IOException {
        try (InputStream in = new FileInputStream(from)) {
            byte[] b = new byte[32768]; int n;
            while ((n = in.read(b)) != -1) to.write(b, 0, n);
        }
    }
}
