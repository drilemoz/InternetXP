package br.com.internetxp;

import org.junit.Test;
import static org.junit.Assert.*;

public final class ToolbarSizingTest {
 @Test public void nineToolsFitCommonScreen(){ assertEquals(43,ToolbarSizing.itemWidth(393,6,38,48,9)); }
 @Test public void narrowScreenKeepsUsableButtons(){ assertEquals(38,ToolbarSizing.itemWidth(320,6,38,48,9)); }
 @Test public void largeScreenLimitsWidth(){ assertEquals(48,ToolbarSizing.itemWidth(1000,6,38,48,9)); }
 @Test(expected=IllegalArgumentException.class) public void rejectZeroItems(){ ToolbarSizing.itemWidth(393,6,38,48,0); }
}
