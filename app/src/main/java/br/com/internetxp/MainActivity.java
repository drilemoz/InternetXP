package br.com.internetxp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.mozilla.geckoview.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Interface XP independente, renderizador real GeckoView. */
public final class MainActivity extends Activity {
 private static final int BLUE = 0xff075ebd, GREY = 0xfff0f0f4, BORDER = 0xffb5c5d4;
 private static final String HOME = "about:blank";
 private static GeckoRuntime runtime;
 private final ArrayList<Tab> tabs = new ArrayList<>();
 private final ArrayList<String> bookmarks = new ArrayList<>(), history = new ArrayList<>();
 private SharedPreferences storage;
 private int active = -1;
 private EditText urlBox;
 private GeckoView view;
 private LinearLayout tabStrip;
 private TextView status, backIcon, forwardIcon;
 private static final class Tab {
  final GeckoSession session;
  String url = HOME, title = "Nova aba";
  boolean back, forward;
  Tab(GeckoSession s) { session = s; }
 }
 private int dp(int n) { return (int)(n * getResources().getDisplayMetrics().density + .5f); }
 private TextView text(String s, int sp, int color) {
  TextView v = new TextView(this); v.setText(s); v.setTextSize(sp); v.setTextColor(color);
  v.setGravity(Gravity.CENTER_VERTICAL); return v;
 }
 private GradientDrawable background(int color, int outline) {
  GradientDrawable d = new GradientDrawable(); d.setColor(color);
  if (outline != 0) d.setStroke(dp(1), outline); return d;
 }
 private void line(LinearLayout root, int size) { View v = new View(this); v.setBackgroundColor(BORDER); root.addView(v,new LinearLayout.LayoutParams(-1,dp(size))); }
 @Override public void onCreate(Bundle state) {
  super.onCreate(state);
  storage = getSharedPreferences("local", MODE_PRIVATE);
  loadList("bookmarks", bookmarks); loadList("history", history);
  createLayout();
  try {
   if (runtime == null) runtime = GeckoRuntime.create(getApplicationContext());
   extensionPrompts(); addTab(HOME);
  } catch (RuntimeException e) { showError(e); }
 }
 private void createLayout() {
  LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(GREY); setContentView(root);
  LinearLayout title = new LinearLayout(this); title.setGravity(Gravity.CENTER_VERTICAL); title.setPadding(dp(12),dp(5),dp(12),dp(5));
  title.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{0xff168df0,0xff004ca9}));
  TextView logo=text("▣",25,Color.WHITE); title.addView(logo,new LinearLayout.LayoutParams(dp(38),dp(42)));
  TextView app=text("Internet XP",21,Color.WHITE); title.addView(app,new LinearLayout.LayoutParams(0,dp(42),1));
  TextView more=text("▾",24,Color.WHITE); more.setGravity(Gravity.CENTER); more.setOnClickListener(v->menu(more,"Ferramentas"));
  title.addView(more,new LinearLayout.LayoutParams(dp(36),dp(42))); root.addView(title); line(root,2);
  HorizontalScrollView menuScroll=new HorizontalScrollView(this); menuScroll.setHorizontalScrollBarEnabled(false);
  LinearLayout menuRow=new LinearLayout(this);
  for(String menu:new String[]{"Arquivo","Editar","Exibir","Favoritos","Ferramentas","Ajuda"}) {
   TextView b=text(menu,14,0xff222222); b.setPadding(dp(11),dp(11),dp(11),dp(11));
   b.setOnClickListener(v->menu(b,menu)); menuRow.addView(b);
  }
  menuScroll.addView(menuRow); root.addView(menuScroll); line(root,2);
  HorizontalScrollView toolScroll=new HorizontalScrollView(this); toolScroll.setHorizontalScrollBarEnabled(false);
  LinearLayout toolbar=new LinearLayout(this); toolbar.setPadding(dp(3),dp(4),dp(3),dp(4));
  backIcon=tool(toolbar,"‹","Voltar",()-> { Tab t=current(); if(t!=null&&t.back)t.session.goBack(); });
  forwardIcon=tool(toolbar,"›","Avançar",()-> { Tab t=current(); if(t!=null&&t.forward)t.session.goForward(); });
  tool(toolbar,"⌂","Início",()->navigate(HOME));
  tool(toolbar,"×","Parar",()-> { Tab t=current(); if(t!=null)t.session.stop(); });
  tool(toolbar,"⌕","Pesquisar",()-> { urlBox.requestFocus(); urlBox.selectAll(); ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).showSoftInput(urlBox,InputMethodManager.SHOW_IMPLICIT); });
  tool(toolbar,"+","Novo",()->addTab(HOME));
  tool(toolbar,"↻","Atualizar",()-> { Tab t=current(); if(t!=null)t.session.reload(); });
  tool(toolbar,"★","Favoritos",this::showBookmarks);
  toolScroll.addView(toolbar); root.addView(toolScroll); line(root,2);
  LinearLayout addressLine=new LinearLayout(this); addressLine.setGravity(Gravity.CENTER_VERTICAL); addressLine.setPadding(dp(6),dp(4),dp(6),dp(4));
  TextView label=text("Endereço",13,0xff333333); label.setPadding(0,0,dp(6),0); addressLine.addView(label);
  urlBox=new EditText(this); urlBox.setSingleLine(); urlBox.setTextSize(13); urlBox.setPadding(dp(8),0,dp(6),0);
  urlBox.setHint("Pesquise ou digite endereço"); urlBox.setBackground(background(Color.WHITE,0xff95a9c4));
  urlBox.setImeOptions(EditorInfo.IME_ACTION_GO); urlBox.setOnEditorActionListener((tv,action,event)-> {
   if(action==EditorInfo.IME_ACTION_GO||(event!=null&&event.getKeyCode()==KeyEvent.KEYCODE_ENTER)) { submitAddress(); return true; }
   return false;
  });
  addressLine.addView(urlBox,new LinearLayout.LayoutParams(0,dp(41),1));
  TextView go=text(" Ir ▸ ",14,BLUE); go.setOnClickListener(v->submitAddress()); addressLine.addView(go);
  root.addView(addressLine); line(root,1);
  HorizontalScrollView tabScroll=new HorizontalScrollView(this); tabScroll.setHorizontalScrollBarEnabled(false); tabScroll.setBackgroundColor(0xffe0e7f1);
  tabStrip=new LinearLayout(this); tabStrip.setPadding(dp(4),dp(3),0,dp(3)); tabScroll.addView(tabStrip); root.addView(tabScroll); line(root,1);
  view=new GeckoView(this); view.setBackgroundColor(Color.WHITE); root.addView(view,new LinearLayout.LayoutParams(-1,0,1)); line(root,2);
  LinearLayout footer=new LinearLayout(this); footer.setGravity(Gravity.CENTER_VERTICAL); footer.setPadding(dp(8),0,dp(8),0);
  status=text("Pronto",12,0xff444444); footer.addView(status,new LinearLayout.LayoutParams(0,dp(34),1));
  TextView addons=text("▧ Extensões",12,BLUE); addons.setOnClickListener(v->showExtensions()); footer.addView(addons);
  root.addView(footer);
 }
 private TextView tool(LinearLayout toolbar,String icon,String caption,Runnable action) {
  LinearLayout cell=new LinearLayout(this); cell.setOrientation(LinearLayout.VERTICAL); cell.setGravity(Gravity.CENTER); cell.setPadding(dp(2),0,dp(2),0);
  TextView symbol=text(icon,31,BLUE); symbol.setTypeface(Typeface.DEFAULT_BOLD); symbol.setGravity(Gravity.CENTER);
  cell.addView(symbol,new LinearLayout.LayoutParams(-1,dp(40)));
  TextView name=text(caption,10,0xff333333); name.setGravity(Gravity.CENTER); cell.addView(name,new LinearLayout.LayoutParams(-1,dp(21)));
  cell.setOnClickListener(v->action.run()); toolbar.addView(cell,new LinearLayout.LayoutParams(dp(65),dp(66))); return symbol;
 }
 private void menu(View anchor,String name) {
  PopupMenu p=new PopupMenu(this,anchor);
  switch(name) {
   case "Arquivo":
    item(p,"Nova aba",()->addTab(HOME)); item(p,"Fechar aba",()->closeTab(active)); item(p,"Compartilhar página",this::share); break;
   case "Editar": item(p,"Copiar endereço",this::copyUrl); break;
   case "Exibir": item(p,"Atualizar",()->{Tab t=current(); if(t!=null)t.session.reload();}); item(p,"Histórico",this::showHistory); break;
   case "Favoritos": item(p,"Adicionar ou remover favorito",this::toggleBookmark); item(p,"Ver favoritos",this::showBookmarks); break;
   case "Ferramentas": item(p,"Extensões",this::showExtensions); item(p,"Downloads",this::showDownloads); item(p,"Limpar histórico local",this::clearHistory); break;
   default:item(p,"Sobre Internet XP",()->new AlertDialog.Builder(this).setTitle("Internet XP 1.0.6").setMessage("GeckoView e interface clássica XP. Projeto independente, sem vínculo com Microsoft ou Mozilla.").setPositiveButton("OK",null).show());
  } p.show();
 }
 private void item(PopupMenu p,String caption,Runnable run) { p.getMenu().add(caption).setOnMenuItemClickListener(m->{run.run();return true;}); }
 private Tab current() { return active>=0&&active<tabs.size()?tabs.get(active):null; }
 private void submitAddress() { navigate(BrowserAddress.resolve(urlBox.getText().toString())); urlBox.clearFocus(); ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(urlBox.getWindowToken(),0); }
 private void navigate(String url) { Tab t=current(); if(t!=null)t.session.loadUri(url); }
 private void addTab(String url) {
  if(tabs.size()>=8) {Toast.makeText(this,"Limite de 8 abas para poupar memória",Toast.LENGTH_SHORT).show();return;}
  GeckoSession session=new GeckoSession(); Tab tab=new Tab(session);
  session.setContentDelegate(new GeckoSession.ContentDelegate() {
   @Override public void onTitleChange(GeckoSession s,String title) { if(title!=null&&!title.trim().isEmpty()) tab.title=title; if(tab==current())drawTabs(); }
   @Override public void onCrash(GeckoSession s) { if(tab==current())status.setText("Falha da página. Reabra a aba."); }
   @Override public void onExternalResponse(GeckoSession s, WebResponse response) { offerDownload(response); }
  });
  session.setNavigationDelegate(new GeckoSession.NavigationDelegate() {
   @Override public void onLocationChange(GeckoSession s,String uri,List<GeckoSession.PermissionDelegate.ContentPermission> perms,Boolean gesture) {
    if(uri==null)return; tab.url=uri; if(tab==current()&&!urlBox.hasFocus())urlBox.setText(uri.equals(HOME)?"":uri);
    if(uri.startsWith("http://")||uri.startsWith("https://"))rememberHistory(uri);
   }
   @Override public void onCanGoBack(GeckoSession s,boolean b) {tab.back=b;if(tab==current())updateArrows();}
   @Override public void onCanGoForward(GeckoSession s,boolean b) {tab.forward=b;if(tab==current())updateArrows();}
  });
  session.setProgressDelegate(new GeckoSession.ProgressDelegate() {
   @Override public void onPageStart(GeckoSession s,String uri) {if(tab==current())status.setText("Carregando…");}
   @Override public void onPageStop(GeckoSession s,boolean success) {if(tab==current())status.setText(success?"Concluído":"Erro ao carregar");}
  });
  session.open(runtime); tabs.add(tab); selectTab(tabs.size()-1); session.loadUri(url);
 }
 private void selectTab(int index) {
  if(index<0||index>=tabs.size())return;
  GeckoSession old=view.releaseSession(); if(old!=null)old.setActive(false);
  active=index; Tab tab=current(); tab.session.setActive(true); view.setSession(tab.session);
  if(!urlBox.hasFocus())urlBox.setText(tab.url.equals(HOME)?"":tab.url);
  updateArrows();drawTabs();
 }
 private void closeTab(int index) {
  if(index<0||index>=tabs.size())return;
  Tab t=tabs.remove(index); if(view.getSession()==t.session)view.releaseSession();t.session.close();
  if(tabs.isEmpty()){active=-1;addTab(HOME);}else selectTab(Math.min(index,tabs.size()-1));
 }
 private void updateArrows() {Tab t=current();backIcon.setTextColor(t!=null&&t.back?BLUE:BORDER);forwardIcon.setTextColor(t!=null&&t.forward?BLUE:BORDER);}
 private void drawTabs() {
  tabStrip.removeAllViews();
  for(int i=0;i<tabs.size();i++) {
   final int index=i; Tab t=tabs.get(i);
   LinearLayout chip=new LinearLayout(this);chip.setGravity(Gravity.CENTER_VERTICAL);chip.setBackground(background(index==active?Color.WHITE:0xffe2e8ef,BORDER));
   String label=t.title.length()>15?t.title.substring(0,14)+"…":t.title;
   TextView name=text(" ▣ "+label,12,0xff222222);name.setSingleLine(true);name.setOnClickListener(v->selectTab(index));
   chip.addView(name,new LinearLayout.LayoutParams(dp(150),dp(32)));
   TextView close=text("×",19,0xff555555);close.setGravity(Gravity.CENTER);close.setContentDescription("Fechar aba");close.setOnClickListener(v->closeTab(index));
   chip.addView(close,new LinearLayout.LayoutParams(dp(28),dp(32)));
   LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(-2,dp(34));params.setMargins(0,0,dp(4),0);tabStrip.addView(chip,params);
  }
 }
 private void loadList(String key,ArrayList<String> list) {
  try {JSONArray a=new JSONArray(storage.getString(key,"[]"));for(int i=0;i<a.length();i++)list.add(a.getString(i));}catch(Exception ignored){}
 }
 private void saveList(String key,ArrayList<String> list) {storage.edit().putString(key,new JSONArray(list).toString()).apply();}
 private void rememberHistory(String url) {history.remove(url);history.add(0,url);while(history.size()>200)history.remove(history.size()-1);saveList("history",history);}
 private void toggleBookmark() {Tab t=current();if(t==null||!t.url.startsWith("http"))return;
  if(!bookmarks.remove(t.url))bookmarks.add(0,t.url);saveList("bookmarks",bookmarks);}
 private void showBookmarks() {
  if(bookmarks.isEmpty()){new AlertDialog.Builder(this).setTitle("Favoritos").setMessage("Nenhum favorito salvo.").setPositiveButton("Adicionar atual",(d,n)->toggleBookmark()).setNegativeButton("Fechar",null).show();return;}
  String[] items=bookmarks.toArray(new String[0]);new AlertDialog.Builder(this).setTitle("Favoritos").setItems(items,(d,n)->navigate(items[n])).setPositiveButton("Adicionar atual",(d,n)->toggleBookmark()).setNegativeButton("Fechar",null).show();
 }
 private void showHistory() {
  if(history.isEmpty()){Toast.makeText(this,"Histórico vazio",Toast.LENGTH_SHORT).show();return;}
  String[] items=history.toArray(new String[0]);new AlertDialog.Builder(this).setTitle("Histórico").setItems(items,(d,n)->navigate(items[n])).setPositiveButton("Limpar",(d,n)->clearHistory()).setNegativeButton("Fechar",null).show();
 }
 private void clearHistory(){new AlertDialog.Builder(this).setTitle("Limpar histórico local?").setMessage("Cookies e cache do GeckoView não serão apagados.").setPositiveButton("Apagar",(d,n)->{history.clear();saveList("history",history);}).setNegativeButton("Cancelar",null).show();}
 private void copyUrl(){Tab t=current();if(t!=null)((ClipboardManager)getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("Endereço",t.url));}
 private void share(){Tab t=current();if(t==null||!t.url.startsWith("http"))return; Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_TEXT,t.url);startActivity(Intent.createChooser(i,"Compartilhar"));}
 private void showDownloads(){try{startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));}catch(Exception e){showError(e);}}
 private void offerDownload(WebResponse response){
  if(response.body!=null)try{response.body.close();}catch(Exception ignored){}
  String uri=response.uri;
  if(uri==null||!(uri.startsWith("https://")||uri.startsWith("http://"))){status.setText("Download não suportado");return;}
  new AlertDialog.Builder(this).setTitle("Baixar arquivo?").setMessage("Origem: "+uri+"\n\nArquivos com login/cookies podem falhar nesta versão.")
   .setPositiveButton("Baixar",(d,n)->{try{
    DownloadManager.Request request=new DownloadManager.Request(Uri.parse(uri));
    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,android.webkit.URLUtil.guessFileName(uri,null,null));
    ((DownloadManager)getSystemService(DOWNLOAD_SERVICE)).enqueue(request);status.setText("Download iniciado");
   }catch(Exception e){showError(e);}}).setNegativeButton("Cancelar",null).show();
 }
 private void extensionPrompts(){
  runtime.getWebExtensionController().setPromptDelegate(new WebExtensionController.PromptDelegate(){
   @Override public GeckoResult<WebExtension.PermissionPromptResponse> onInstallPromptRequest(WebExtension ext,String[] permissions,String[] origins,String[] collected){
    GeckoResult<WebExtension.PermissionPromptResponse> r=new GeckoResult<>();
    new AlertDialog.Builder(MainActivity.this).setTitle("Permitir extensão?").setMessage(
     "Extensão: "+ext.id+"\nPermissões: "+Arrays.toString(permissions)+"\nSites: "+Arrays.toString(origins)+"\nDados: "+Arrays.toString(collected))
     .setPositiveButton("Permitir",(d,n)->r.complete(new WebExtension.PermissionPromptResponse(true,false,false)))
     .setNegativeButton("Recusar",(d,n)->r.complete(new WebExtension.PermissionPromptResponse(false,false,false)))
     .setOnCancelListener(d->r.complete(new WebExtension.PermissionPromptResponse(false,false,false))).show();
    return r;
   }
   @Override public GeckoResult<AllowOrDeny> onOptionalPrompt(WebExtension ext,String[] permissions,String[] origins,String[] collected){return reviewPermissions(ext,permissions,origins,collected);}
   @Override public GeckoResult<AllowOrDeny> onUpdatePrompt(WebExtension ext,String[] permissions,String[] origins,String[] collected){return reviewPermissions(ext,permissions,origins,collected);}
  });
 }
 private GeckoResult<AllowOrDeny> reviewPermissions(WebExtension ext,String[] p,String[] o,String[] c){
  GeckoResult<AllowOrDeny> r=new GeckoResult<>();
  new AlertDialog.Builder(this).setTitle("Novas permissões: "+ext.id).setMessage(Arrays.toString(p)+"\nSites: "+Arrays.toString(o)+"\nDados: "+Arrays.toString(c))
   .setPositiveButton("Permitir",(d,n)->r.complete(AllowOrDeny.ALLOW)).setNegativeButton("Recusar",(d,n)->r.complete(AllowOrDeny.DENY))
   .setOnCancelListener(d->r.complete(AllowOrDeny.DENY)).show();return r;
 }
 private void showExtensions(){if(runtime==null)return;
  runtime.getWebExtensionController().list().accept(list->runOnUiThread(()->{
   String[] options=new String[list.size()+2];options[0]="+ Instalar XPI por URL HTTPS";options[1]="Catálogo Mozilla Add-ons";
   for(int i=0;i<list.size();i++){WebExtension x=list.get(i);options[i+2]=(x.metaData.enabled?"✓ ":"○ ")+(x.metaData.name==null?x.id:x.metaData.name);}
   new AlertDialog.Builder(this).setTitle("Extensões Firefox").setItems(options,(d,n)->{
    if(n==0)installExtension();else if(n==1)navigate("https://addons.mozilla.org/android/");else extensionActions(list.get(n-2));
   }).setNegativeButton("Fechar",null).show();
  }),e->runOnUiThread(()->showError(e)));
 }
 private void extensionActions(WebExtension x){
  new AlertDialog.Builder(this).setTitle(x.metaData.name==null?x.id:x.metaData.name)
   .setItems(new String[]{x.metaData.enabled?"Desativar":"Ativar","Desinstalar"},(d,n)->{
    if(n==1){new AlertDialog.Builder(this).setTitle("Desinstalar extensão?")
     .setPositiveButton("Remover",(d2,n2)->runtime.getWebExtensionController().uninstall(x).accept(v->runOnUiThread(this::showExtensions),e->runOnUiThread(()->showError(e))))
     .setNegativeButton("Cancelar",null).show();
    } else {
     GeckoResult<WebExtension> r=x.metaData.enabled?runtime.getWebExtensionController().disable(x,WebExtensionController.EnableSource.USER):runtime.getWebExtensionController().enable(x,WebExtensionController.EnableSource.USER);
     r.accept(v->runOnUiThread(this::showExtensions),e->runOnUiThread(()->showError(e)));
    }
   }).show();
 }
 private void installExtension(){
  EditText link=new EditText(this);link.setSingleLine();link.setHint("https://.../complemento.xpi");
  new AlertDialog.Builder(this).setTitle("Instalar extensão Mozilla assinada").setMessage("Cole o link direto HTTPS para o pacote XPI assinado.").setView(link)
   .setPositiveButton("Instalar",(d,n)->{
    String uri=link.getText().toString().trim();if(!uri.startsWith("https://")){Toast.makeText(this,"É obrigatório HTTPS",Toast.LENGTH_LONG).show();return;}
    runtime.getWebExtensionController().install(uri).accept(x->runOnUiThread(()->{status.setText("Extensão instalada");showExtensions();}),e->runOnUiThread(()->showError(e)));
   }).setNegativeButton("Cancelar",null).show();
 }
 private void showError(Throwable e){new AlertDialog.Builder(this).setTitle("Falha").setMessage(String.valueOf(e.getMessage())).setPositiveButton("OK",null).show();}
 @Override public void onBackPressed(){Tab t=current();if(t!=null&&t.back)t.session.goBack();else new AlertDialog.Builder(this).setTitle("Sair do Internet XP?").setPositiveButton("Sair",(d,n)->finish()).setNegativeButton("Cancelar",null).show();}
 @Override protected void onDestroy(){if(view!=null)view.releaseSession();for(Tab t:tabs)t.session.close();tabs.clear();super.onDestroy();}
}
