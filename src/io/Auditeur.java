package io;

public interface Auditeur {

	/**
	 * Fonction exécutée lorsque survient un évènement au niveau de l'interface
	 * graphique.
	 *
	 * @param nomElement le nom de l'élément à l'origine de l'évènement
	 * @param action     l'action effectuée au niveau de l'interface graphique
	 * @param valeur     une valeur associée à l'évènement si c'est possible, la
	 *                   référence null dans le cas contraire
	 */
	void executerAction(Fenetre instance, String nomElement, ActionFenetre action, String valeur);

}
