# Regras Internet XP
- Versionamento sequencial em toda alteração: subir versionCode e versionName e sincronizar README/CHANGELOG/docs.
- GeckoView é obrigatório; não trocar por WebView.
- Fidelidade à interface XP do screenshot do Explorador XP.
- Publicar APK direto no GitHub Release: `Internet XP.v1.0.1.apk`, sem upload-artifact/ZIP.
- Secrets: KEYSTORE_BASE64, KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD. Nunca inserir credenciais em commit.
- Não declarar APK disponível antes de build bem-sucedido e asset da Release confirmado.
