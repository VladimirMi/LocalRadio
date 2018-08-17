package io.github.vladimirmi.localradio.custom;

import android.widget.AutoCompleteTextView;

import java.util.List;

/**
 * Created by Vladimir Mikhalev 16.07.2018.
 */
public class TextViewValidator<T> implements AutoCompleteTextView.Validator {

    private final List<T> list;

    public TextViewValidator(List<T> list) {
        this.list = list;
    }

    @Override
    public boolean isValid(CharSequence text) {
        if (text.toString().isEmpty()) return false;
        for (T element : list) {
            if (element.toString().equals(text.toString())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public CharSequence fixText(CharSequence invalidText) {
        if (invalidText.toString().trim().isEmpty()) return "";
        int maxCharEquals = 0;
        int elementIndex = 0;

        for (int idx = 0; idx < list.size(); idx++) {
            String element = list.get(idx).toString();
            int charEquals = 0;

            int minLength = Math.min(element.length(), invalidText.length());
            for (int i = 0; i < minLength; i++) {
                char actual = invalidText.charAt(i);
                char expected = element.charAt(i);

                if (actual == expected) {
                    charEquals++;
                    if (charEquals == minLength) {
                        return element;
                    }
                }
                if (charEquals > maxCharEquals) {
                    maxCharEquals = charEquals;
                    elementIndex = idx;
                }
            }
        }

        return list.isEmpty() ? "" : list.get(elementIndex).toString();
    }
}
