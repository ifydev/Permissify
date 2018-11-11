package me.innectic.permissify.api.util;

import me.innectic.permissify.api.PermissifyConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Innectic
 * @since 11/09/2018
 */
public class HelpUtil {

    public static Optional<List<String>> getHelpInformationAtPage(int page) {
        if (page > PermissifyConstants.MAX_PAGES) return Optional.empty();

        List<String> items = new ArrayList<>();

        int startingIndex = page * PermissifyConstants.LINES_PER_PAGE;
        for (int i = 0; i < PermissifyConstants.LINES_PER_PAGE; i++) {
            int current = startingIndex + i;
            if (current == PermissifyConstants.PERMISSIFY_HELP_PAGES.size()) break;
            items.add(PermissifyConstants.PERMISSIFY_HELP_PAGES.get(current));
        }
        return Optional.of(items);
    }
}
