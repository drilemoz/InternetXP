package br.com.internetxp;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Estilo reutilizável para todos os diálogos normais do Internet XP.
 *  Conserva callbacks, cancelamento e escolhas da API oficial de AlertDialog.
 */
final class XpDialogs {
    private XpDialogs() { }
    private static final int BLUE = 0xff0d68c8;
    private static final int DARK = 0xff20334b;
    private static final int BORDER = 0xffccdaeb;

    private static int dp(Context context, int value) {
        return (int) (value * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    private static GradientDrawable fill(Context context, int color, int stroke, int radius) {
        GradientDrawable background = new GradientDrawable();
        background.setColor(color);
        background.setCornerRadius(dp(context, radius));
        if (stroke != 0) background.setStroke(dp(context, 1), stroke);
        return background;
    }

    static final class Builder extends AlertDialog.Builder {
        private final Context context;

        Builder(Context context) {
            super(context);
            this.context = context;
        }

        @Override public AlertDialog.Builder setTitle(CharSequence title) {
            // Identidade visual consistente sem depender do tema OEM/MIUI.
            LinearLayout header = new LinearLayout(context);
            header.setOrientation(LinearLayout.VERTICAL);
            header.setPadding(dp(context, 20), dp(context, 17), dp(context, 20), dp(context, 15));
            GradientDrawable gradient = new GradientDrawable(
                    GradientDrawable.Orientation.LEFT_RIGHT, new int[]{BLUE, 0xff2a86dd});
            float r = dp(context, 17);
            gradient.setCornerRadii(new float[]{r, r, r, r, 0, 0, 0, 0});
            header.setBackground(gradient);
            TextView signature = new TextView(context);
            signature.setText("INTERNET XP");
            signature.setTextColor(0xffd9ebff);
            signature.setTextSize(10);
            signature.setTypeface(Typeface.DEFAULT_BOLD);
            header.addView(signature);
            TextView name = new TextView(context);
            name.setText(title);
            name.setTextColor(Color.WHITE);
            name.setTextSize(19);
            name.setTypeface(Typeface.DEFAULT_BOLD);
            name.setPadding(0, dp(context, 4), 0, 0);
            header.addView(name);
            return super.setCustomTitle(header);
        }

        @Override public AlertDialog.Builder setView(View view) {
            // O EditText isolado não fica colado às bordas. Views personalizadas
            // do menu principal conservam sua hierarquia original.
            if (view instanceof EditText) {
                FrameLayout container = new FrameLayout(context);
                container.setPadding(dp(context, 18), dp(context, 8), dp(context, 18), dp(context, 8));
                container.addView(view, new FrameLayout.LayoutParams(-1, -2));
                return super.setView(container);
            }
            return super.setView(view);
        }

        @Override public AlertDialog create() {
            AlertDialog dialog = super.create();
            dialog.setOnShowListener(ignored -> style(dialog));
            return dialog;
        }

        @Override public AlertDialog show() {
            AlertDialog dialog = create();
            dialog.show();
            return dialog;
        }
    }

    private static void style(AlertDialog dialog) {
        Window window = dialog.getWindow();
        if (window == null) return;
        Context context = dialog.getContext();
        int screenWidth = context.getResources().getConfiguration().screenWidthDp;
        int widthDp = Math.min(450, Math.max(260, screenWidth - 24));
        window.setBackgroundDrawable(fill(context, Color.WHITE, BORDER, 17));
        window.setLayout(dp(context, widthDp), WindowManager.LayoutParams.WRAP_CONTENT);
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        window.setDimAmount(0.38f);
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setWindowAnimations(android.R.style.Animation_Dialog);

        TextView message = dialog.findViewById(android.R.id.message);
        if (message != null) {
            message.setTextColor(DARK);
            message.setTextSize(14);
            message.setLineSpacing(dp(context, 3), 1.0f);
        }
        styleButton(dialog.getButton(AlertDialog.BUTTON_POSITIVE), context, true);
        styleButton(dialog.getButton(AlertDialog.BUTTON_NEGATIVE), context, false);
        styleButton(dialog.getButton(AlertDialog.BUTTON_NEUTRAL), context, false);
    }

    private static void styleButton(Button button, Context context, boolean primary) {
        if (button == null) return;
        button.setAllCaps(false);
        button.setTextSize(13);
        button.setTypeface(Typeface.DEFAULT_BOLD);
        button.setMinHeight(dp(context, 44));
        if (primary) {
            String label = button.getText().toString().toLowerCase(java.util.Locale.ROOT);
            boolean danger = label.contains("excluir") || label.contains("apagar") ||
                    label.contains("limpar") || label.contains("remover");
            button.setTextColor(Color.WHITE);
            button.setBackground(fill(context, danger ? 0xffbb4046 : BLUE, 0, 10));
        } else {
            button.setTextColor(BLUE);
            button.setBackground(fill(context, 0xfff4f8ff, BORDER, 10));
        }
        ViewGroup.LayoutParams params = button.getLayoutParams();
        if (params instanceof LinearLayout.LayoutParams) {
            LinearLayout.LayoutParams layout = (LinearLayout.LayoutParams) params;
            layout.leftMargin = dp(context, 4);
            layout.rightMargin = dp(context, 4);
            layout.bottomMargin = dp(context, 5);
            button.setLayoutParams(layout);
        }
    }
}
