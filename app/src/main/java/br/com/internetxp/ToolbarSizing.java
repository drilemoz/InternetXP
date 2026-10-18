package br.com.internetxp;

/** Largura adaptativa em px, mantendo rolagem horizontal em telas estreitas. */
final class ToolbarSizing {
 private ToolbarSizing() { }
 static int itemWidth(int screenPx,int horizontalPaddingPx,int minimumPx,int maximumPx,int items) {
  if (items<=0 || minimumPx<=0 || maximumPx<minimumPx) throw new IllegalArgumentException("Dimensões inválidas");
  int available=Math.max(0,screenPx-horizontalPaddingPx);
  return Math.max(minimumPx,Math.min(maximumPx,available/items));
 }
}
