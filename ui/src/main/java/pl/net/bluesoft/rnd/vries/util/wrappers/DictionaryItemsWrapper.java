package pl.net.bluesoft.rnd.vries.util.wrappers;

import pl.net.bluesoft.rnd.vries.wrappers.DictionaryItem;

import java.util.LinkedList;
import java.util.List;

/**
 * A container for report dictionary items.
 */
public class DictionaryItemsWrapper {
    private List<DictionaryItem> items;

    public DictionaryItemsWrapper() {
        items = new LinkedList<DictionaryItem>();
    }

    public void addItem(String[] columns) {
        items.add(new DictionaryItem(columns[0], columns[1], columns));
    }

    public List<DictionaryItem> getItems() {
        return items;
    }
}
