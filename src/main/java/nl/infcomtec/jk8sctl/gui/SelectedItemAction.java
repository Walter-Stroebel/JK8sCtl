/*
 *  Copyright (c) 2018 by Walter Stroebel and InfComTec.
 */
package nl.infcomtec.jk8sctl.gui;

import nl.infcomtec.jk8sctl.Metadata;

/**
 * What to do with a selected item.
 *
 * @author walter
 */
public interface SelectedItemAction {

    /**
     * Text to display above the selection tree.
     *
     * @return Text to display above the selection tree.
     */
    String topLine();

    /**
     * Do something with the selected item.
     *
     * @param item Item the user selected.
     * @return true if done (close the dialog), false to continue.
     */
    boolean withSelected(Metadata item);
}
