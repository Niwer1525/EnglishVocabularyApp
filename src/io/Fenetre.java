package io;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * Cette classe propose différentes fonctions permettant de créer une interface
 * graphique et d'interagir avec cette dernière.
 *
 * @author Arnaud Comblin
 * @version 1.6
 */
public class Fenetre implements ActionListener {

	private JFrame cadre;
	private Panneau panneau;
	private final int NB_LIGNES;
	private final int NB_COLONNES;
	private Map<String, ElementFenetre> elementsParNom = new HashMap<String, ElementFenetre>();
	private Map<Position, ElementFenetre> elementsParPosition = new HashMap<Position, ElementFenetre>();
	private List<ElementFenetre> elements = new LinkedList<ElementFenetre>();
	private Map<String, Image> cheminsEtImages = new HashMap<String, Image>();
	private Auditeur auditeur;
	private Font police = new Font(Theme.NOM_POLICE, Font.PLAIN, Theme.TAILLE_POLICE);
	private Color couleurTexte = Theme.COULEUR_TEXTE;
	private int margeVerticale = 0, margeHorizontale = 0;
	private java.util.Timer timer = null;
	private long debut = System.nanoTime();

	/**
	 * Crée une fenêtre permettant d'organiser ses élements à l'aide d'une grille.
	 * Il faut exécuter cette fonction en premier lieu.
	 * 
	 * @since 1.0
	 */
	public Fenetre() {
		this(Theme.TITRE, Theme.LARGEUR, Theme.HAUTEUR, Theme.NB_COLONNES, Theme.NB_LIGNES);
	}

	/**
	 * Crée une fenêtre permettant destinée à afficher une scène 2D. Il faut
	 * exécuter cette fonction en premier lieu.
	 * 
	 * @param titre   le titre principal de la fenêtre
	 * @param largeur la largeur exprimée en pixels de l'intérieur de la fenêtre
	 * @param hauteur la hauteur exprimée en pixels de l'intérieur de la fenêtre
	 * @since 1.0
	 */
	public Fenetre(String titre, int largeur, int hauteur) {
		this(titre, largeur, hauteur, 1, 1);
	}

	/**
	 * Crée une fenêtre permettant d'organiser ses élements à l'aide d'une grille.
	 * Il faut exécuter cette fonction en premier lieu.
	 * 
	 * @param titre      le titre principal de la fenêtre
	 * @param largeur    la largeur exprimée en pixels de l'intérieur de la fenêtre
	 * @param hauteur    la hauteur exprimée en pixels de l'intérieur de la fenêtre
	 * @param nbColonnes le nombre de colonnes de la grille
	 * @param nbLignes   le nombre de lignes de la grille
	 * @since 1.0
	 */
	public Fenetre(String titre, int largeur, int hauteur, int nbColonnes, int nbLignes) {
		cadre = new JFrame(titre);
		cadre.setFocusable(true);
		cadre.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent windowEvent) {
				if (timer != null) {
					timer.cancel();
				}
				System.exit(0);
			}
		});
		// cadre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		cadre.setResizable(false);
		cadre.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent evenement) {
				signalerEvenement(cadre.getTitle(), ActionFenetre.PRESSION_TOUCHE,
						String.valueOf(KeyEvent.getKeyText(evenement.getKeyCode())));
			}
		});

		panneau = new Panneau();
		panneau.setLayout(null);
		panneau.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evenement) {
				ActionFenetre action = (evenement.getButton() == MouseEvent.BUTTON3) ? ActionFenetre.CLIC_DROIT
						: ActionFenetre.CLIC_GAUCHE;
				signalerEvenement(cadre.getTitle(), action, evenement.getX() + "," + evenement.getY());
			}
		});
		cadre.add(panneau);

		setCouleurArrierePlan(Theme.COULEUR_ARRIERE_PLAN);
		setDimensions(largeur, hauteur);

		NB_LIGNES = Math.min(Math.max(nbLignes, 1), panneau.getHeight() / 10);
		NB_COLONNES = Math.min(Math.max(nbColonnes, 1), panneau.getWidth() / 10);
	}

	/**
	 * Définit la fonction à exécuter lorsqu'un évènement survient au niveau de
	 * l'interface graphique (par exemple : clic sur un bouton, pression d'une
	 * touche du clavier ...).
	 * 
	 * @param fonction la fonction à exécuter pour traiter les évènements
	 * @since 1.4
	 */

	public void setAuditeur(Auditeur auditeur) {
		if (auditeur == null) {
			afficherStackTrace("L'auditeur n'est pas valide (reference null) !");
		}
		this.auditeur = auditeur;
	}

	/**
	 * Définit la fonction à exécuter à intervalle régulier.
	 * 
	 * @param fonction  la fonction à exécuter pour traiter les évènements
	 * @param delaiEnMs le délai exprimé en millisecondes qui doit séparer chaque
	 *                  exécution de la fonction
	 * @since 1.4
	 */
	public void setAuditeur(Auditeur auditeur, int delaiEnMs) {
		setAuditeur(auditeur);
		java.util.TimerTask tache = new java.util.TimerTask() {
			@Override
			public void run() {
				signalerEvenement(cadre.getTitle(), ActionFenetre.MINUTEUR,
						String.valueOf((System.nanoTime() - debut) / 1000000));
			}
		};
		timer = new java.util.Timer();
		timer.scheduleAtFixedRate(tache, 0, delaiEnMs);
	}

	/**
	 * Affiche la fenêtre. Cette fonction doit être exécutée une fois tous les
	 * éléments ajoutés. Dans le cas contraire, certains éléments pourraient ne pas
	 * s'afficher correctement.
	 * 
	 * @since 1.0
	 */
	public void afficher() {
		cadre.pack();
		cadre.setVisible(true);
	}

	/**
	 * Modifie le titre principal de la fenêtre.
	 * 
	 * @param titre le nouveau titre
	 * @since 1.0
	 */
	public void setTitre(String titre) {
		cadre.setTitle(titre);
	}

	private void setDimensions(int largeur, int hauteur) {
		Dimension resolutionEcran = Toolkit.getDefaultToolkit().getScreenSize();
		largeur = Math.min(Math.max(160, largeur), (int) resolutionEcran.getWidth());
		hauteur = Math.min(Math.max(120, hauteur), (int) resolutionEcran.getHeight());
		panneau.setPreferredSize(new Dimension(largeur, hauteur));
		cadre.pack();
	}

	/**
	 * Modifie la couleur de l'arrière-plan de la fenêtre.
	 * 
	 * @param couleur la nouvelle couleur de l'arrière-plan
	 * @since 1.0
	 */
	public void setCouleurArrierePlan(Color couleur) {
		panneau.setBackground(couleur);
	}

	/**
	 * Modifie la couleur du texte affiché dans la fenêtre.
	 * 
	 * @param couleur la nouvelle couleur du texte
	 * @since 1.0
	 */
	public void setCouleurTexte(Color couleur) {
		couleurTexte = couleur;
		for (ElementFenetre element : elements) {
			element.setCouleurTexte(couleur);
		}
	}

	/**
	 * Modifie la taille du texte affiché dans la fenêtre.
	 * 
	 * @param taille la nouvelle taille du texte
	 * @since 1.0
	 */
	public void setTailleTexte(int taille) {
		police = new Font(Theme.NOM_POLICE, Font.PLAIN, taille);
		for (ElementFenetre element : elements) {
			element.setPolice(police);
		}
	}

	/**
	 * Définit une marge de séparation entre chaque élément de l'interface graphique
	 * et le bord de leur cellule.
	 * 
	 * @param marge la grandeur de la marge exprimée en pixels à appliquer en haut,
	 *              à droite, en bas et à gauche de chaque élément
	 * @since 1.1
	 */
	public void setMargesElements(int marge) {
		setMargesElements(marge, marge);
	}

	/**
	 * Définit des marges de séparation entre chaque élément de l'interface
	 * graphique et le bord de leur cellule.
	 * 
	 * @param margeVerticale   la grandeur de la marge exprimée en pixels à
	 *                         appliquer en haut et en bas de chaque élément
	 * @param margeHorizontale la grandeur de la marge exprimée en pixels à
	 *                         appliquer à gauche et à droite de chaque élément
	 * @since 1.1
	 */
	public void setMargesElements(int margeVerticale, int margeHorizontale) {
		this.margeVerticale = Math.min(Math.max(0, margeVerticale), getHauteurCellule() / 2);
		this.margeHorizontale = Math.min(Math.max(0, margeHorizontale), getLargeurCellule() / 2);
		for (Position position : elementsParPosition.keySet()) {
			ElementFenetre element = elementsParPosition.get(position);
			positionnerElement(element, position);
		}
	}

	/**
	 * Ajoute une étiquette permettant d'afficher du texte dans une cellule de la
	 * grille de la fenêtre.
	 * 
	 * @param nomElement un nom unique permettant d'identifier l'étiquette
	 * @param texte      le texte à afficher initialement dans l'étiquette
	 * @param ligne      l'indice de la ligne à laquelle doit être affichée
	 *                   l'étiquette
	 * @param colonne    l'indice de la colonne à laquelle doit être affichée
	 *                   l'étiquette
	 * @since 1.0
	 */
	public void ajouterEtiquette(String nomElement, String texte, int ligne, int colonne) {
		ajouterEtiquette(nomElement, texte, AlignementTexte.GAUCHE, ligne, colonne);
	}

	/**
	 * Ajoute une étiquette permettant d'afficher du texte dans une cellule de la
	 * grille de la fenêtre.
	 * 
	 * @param nomElement un nom unique permettant d'identifier l'étiquette
	 * @param texte      le texte à afficher initialement dans l'étiquette
	 * @param alignement l'alignement utilisé pour afficher le texte
	 * @param ligne      l'indice de la ligne à laquelle doit être affichée
	 *                   l'étiquette
	 * @param colonne    l'indice de la colonne à laquelle doit être affichée
	 *                   l'étiquette
	 * @since 1.2
	 */
	public void ajouterEtiquette(String nomElement, String texte, AlignementTexte alignement, int ligne, int colonne) {
		verifierArguments(nomElement, ligne, colonne);
		int alignementSwing = SwingConstants.LEFT;
		if (alignement == AlignementTexte.CENTRE) {
			alignementSwing = SwingConstants.CENTER;
		} else if (alignement == AlignementTexte.DROITE) {
			alignementSwing = SwingConstants.RIGHT;
		}
		ajouterElement(new Etiquette(nomElement, new JLabel(texte, alignementSwing)), nomElement, ligne, colonne);
	}

	/**
	 * Ajoute une zone de saisie permettant à l'utilisateur de saisir du texte dans
	 * une cellule de la grille de la fenêtre.
	 * 
	 * @param nomElement un nom unique permettant d'identifier la zone de saisie
	 * @param texte      le texte à afficher initialement dans la zone de saisie
	 * @param ligne      l'indice de la ligne à laquelle doit être affichée la zone
	 *                   de saisie
	 * @param colonne    l'indice de la colonne à laquelle doit être affichée la
	 *                   zone de saisie
	 * @since 1.0
	 */
	public void ajouterZoneSaisie(String nomElement, String texte, int ligne, int colonne) {
		verifierArguments(nomElement, ligne, colonne);
		ajouterElement(new ZoneSaisie(nomElement, new JTextField(texte)), nomElement, ligne, colonne);
	}

	/**
	 * Ajoute une liste déroulante permettant à l'utilisateur de sélectionner une
	 * valeur parmi plusieurs dans une cellule de la grille de la fenêtre.
	 * 
	 * @param nomElement un nom unique permettant d'identifier la liste déroulante
	 * @param texte      l'option qui doit être sélectionnée par défaut parmi celles
	 *                   spécifiées dans le paramètre {@code valeurs}
	 * @param valeurs    les différentes options à afficher initialement dans la
	 *                   liste déroulante (celles-ci doivent être séparées par un
	 *                   saut de ligne '\n')
	 * @param ligne      l'indice de la ligne à laquelle doit être affichée la liste
	 *                   déroulante
	 * @param colonne    l'indice de la colonne à laquelle doit être affichée la
	 *                   liste déroulante
	 * @since 1.4
	 */
	public void ajouterListeDeroulante(String nomElement, String texte, String valeurs, int ligne, int colonne) {
		verifierArguments(nomElement, ligne, colonne);
		ElementVisible element = new ListeDeroulante(nomElement, new JComboBox<String>(), valeurs);
		element.setValeur(texte);
		ajouterElement(element, nomElement, ligne, colonne);
	}

	/**
	 * Ajoute un bouton permettant à l'utilisateur d'exécuter une action.
	 * 
	 * @param nomElement un nom unique permettant d'identifier le bouton
	 * @param texte      le texte à afficher initialement sur le bouton
	 * @param ligne      l'indice de la ligne à laquelle doit être affiché le bouton
	 * @param colonne    l'indice de la colonne à laquelle doit être affiché le
	 *                   bouton
	 * @since 1.0
	 */
	public void ajouterBouton(String nomElement, String texte, int ligne, int colonne) {
		ajouterBouton(nomElement, texte, null, ligne, colonne);
	}

	/**
	 * Ajoute un bouton permettant à l'utilisateur d'exécuter une action.
	 * 
	 * @param nomElement  un nom unique permettant d'identifier le bouton
	 * @param texte       le texte à afficher initialement sur le bouton
	 * @param cheminImage le chemin d'accès à l'image (par exemple : "img\logo.png")
	 * @param ligne       l'indice de la ligne à laquelle doit être affiché le
	 *                    bouton
	 * @param colonne     l'indice de la colonne à laquelle doit être affiché le
	 *                    bouton
	 * @since 1.5
	 */
	public void ajouterBouton(String nomElement, String texte, String cheminImage, int ligne, int colonne) {
		verifierArguments(nomElement, ligne, colonne);
		JButton bouton = new JButton(texte);
		bouton.addActionListener(this);
		if (cheminImage != null) {
			Image image = getImage(cheminImage);
			int largeurImage = image.getWidth(null);
			int hauteurImage = image.getHeight(null);
			double ratioLargeur = (double) (getLargeurCellule() - margeHorizontale - 10) / largeurImage;
			double ratioHauteur = (double) (getHauteurCellule() - margeVerticale - 10) / hauteurImage;
			double ratio = Math.min(ratioLargeur, ratioHauteur);
			largeurImage *= ratio;
			hauteurImage *= ratio;
			image = image.getScaledInstance(largeurImage, hauteurImage, Image.SCALE_SMOOTH);
			bouton.setIcon(new ImageIcon(image));
		}
		ajouterElement(new Bouton(nomElement, bouton), nomElement, ligne, colonne);
	}

	/**
	 * Ajoute une case à cocher permettant à l'utilisateur d'indiquer un choix.
	 * 
	 * @param nomElement un nom unique permettant d'identifier la case à cocher
	 * @param texte      le texte à afficher à côté de la case à cocher
	 * @param ligne      l'indice de la ligne à laquelle doit être affichée la case
	 *                   à cocher
	 * @param colonne    l'indice de la colonne à laquelle doit être affichée la
	 *                   case à cocher
	 * @since 1.5
	 */
	public void ajouterCaseCocher(String nomElement, String texte, int ligne, int colonne) {
		ajouterCaseCocher(nomElement, texte, false, ligne, colonne);
	}

	/**
	 * Ajoute une case à cocher permettant à l'utilisateur d'indiquer un choix.
	 * 
	 * @param nomElement un nom unique permettant d'identifier la case à cocher
	 * @param texte      le texte à afficher à côté de la case à cocher
	 * @param cocher     indique si la case est initialement cochée ou non
	 * @param ligne      l'indice de la ligne à laquelle doit être affichée la case
	 *                   à cocher
	 * @param colonne    l'indice de la colonne à laquelle doit être affichée la
	 *                   case à cocher
	 * @since 1.5
	 */
	public void ajouterCaseCocher(String nomElement, String texte, boolean cocher, int ligne, int colonne) {
		verifierArguments(nomElement, ligne, colonne);
		ajouterElement(new CaseCocher(nomElement, new JCheckBox(texte, cocher)), nomElement, ligne, colonne);
	}

	/**
	 * Ajoute un bouton radio permettant à l'utilisateur d'indiquer un et un seul
	 * choix parmi ceux appartenant à un même groupe.
	 * 
	 * @param nomGroupe  un nom unique permettant d'identifier le groupe des boutons
	 *                   radios
	 * @param nomElement un nom unique permettant d'identifier le bouton radio
	 * @param texte      le texte à afficher à côté du bouton radio
	 * @param ligne      l'indice de la ligne à laquelle doit être affichée le
	 *                   bouton radio
	 * @param colonne    l'indice de la colonne à laquelle doit être affichée le
	 *                   bouton radio
	 * @since 1.5
	 */
	public void ajouterBoutonRadio(String nomGroupe, String nomElement, String texte, int ligne, int colonne) {
		ajouterBoutonRadio(nomGroupe, nomElement, texte, false, ligne, colonne);
	}

	/**
	 * Ajoute un bouton radio permettant à l'utilisateur d'indiquer un et un seul
	 * choix parmi ceux appartenant à un même groupe.
	 * 
	 * @param nomGroupe  un nom unique permettant d'identifier le groupe des boutons
	 *                   radios
	 * @param nomElement un nom unique permettant d'identifier le bouton radio
	 * @param texte      le texte à afficher à côté du bouton radio
	 * @param cocher     indique si le bouton radio est initialement coché ou non
	 * @param ligne      l'indice de la ligne à laquelle doit être affichée le
	 *                   bouton radio
	 * @param colonne    l'indice de la colonne à laquelle doit être affichée le
	 *                   bouton radio
	 * @since 1.5
	 */
	public void ajouterBoutonRadio(String nomGroupe, String nomElement, String texte, boolean cocher, int ligne,
			int colonne) {
		if (nomGroupe == null || nomGroupe.isBlank()) {
			afficherStackTrace("Le nom \"" + nomGroupe + "\" n'est pas valide !");
		}
		verifierArguments(nomElement, ligne, colonne);
		ElementFenetre element = elementsParNom.get(nomGroupe);
		GroupeBoutons groupeBoutons = null;
		if (element == null) {
			groupeBoutons = new GroupeBoutons(nomGroupe, new ButtonGroup());
			ajouterGroupe(groupeBoutons, nomGroupe);
		} else if (element instanceof GroupeBoutons) {
			groupeBoutons = (GroupeBoutons) element;
		} else {
			afficherStackTrace("Le nom \"" + nomGroupe + "\" est deja utilise par un autre element !");
		}
		BoutonRadio boutonRadio = new BoutonRadio(nomElement, new JRadioButton(texte, cocher));
		groupeBoutons.ajouter(boutonRadio);
		ajouterElement(boutonRadio, nomElement, ligne, colonne);
	}

	private void verifierArguments(String nomElement, int ligne, int colonne) {
		verifierSiNomValide(nomElement);
		verifierSiPositionValide(new Position(ligne, colonne));
	}

	private void verifierSiNomValide(String nomElement) {
		if (nomElement == null || nomElement.isBlank()) {
			afficherStackTrace("Le nom \"" + nomElement + "\" n'est pas valide !");
		}
		if (elementsParNom.containsKey(nomElement)) {
			afficherStackTrace("Le nom \"" + nomElement + "\" existe est deja utilise par un autre element !");
		}
	}

	private void verifierSiPositionValide(Position positionElement) {
		if (positionElement.j < 0 || positionElement.j >= NB_COLONNES) {
			afficherStackTrace("La colonne " + positionElement.j + " n'existe pas !");
		}
		if (positionElement.i < 0 || positionElement.i >= NB_LIGNES) {
			afficherStackTrace("La ligne " + positionElement.i + " n'existe pas !");
		}
		if (elementsParPosition.containsKey(positionElement)) {
			afficherStackTrace("La position " + positionElement + " est deja utilisee par un autre element !");
		}
	}

	private void ajouterGroupe(GroupeBoutons groupe, String nom) {
		elementsParNom.put(nom, groupe);
		elements.add(groupe);
	}

	private void ajouterElement(ElementVisible element, String nom, int ligne, int colonne) {
		Position position = new Position(ligne, colonne);
		positionnerElement(element, position);
		element.setPolice(police);
		element.setCouleurTexte(couleurTexte);
		elementsParNom.put(nom, element);
		elementsParPosition.put(position, element);
		elements.add(element);
		panneau.add((Component) element.getComposant());
	}

	private int getHauteurCellule() {
		return panneau.getHeight() / NB_LIGNES;
	}

	private int getLargeurCellule() {
		return panneau.getWidth() / NB_COLONNES;
	}

	private void positionnerElement(ElementFenetre element, Position position) {
		int largeurCellule = getLargeurCellule();
		int hauteurCellule = getHauteurCellule();
		int x = position.j * largeurCellule + margeHorizontale;
		int y = position.i * hauteurCellule + margeVerticale;
		int largeur = largeurCellule - 2 * margeHorizontale;
		int hauteur = hauteurCellule - 2 * margeVerticale;
		element.positionner(x, y, largeur, hauteur);
	}

	/**
	 * Retourne la valeur actuelle de l'élément spécifié.
	 * 
	 * @param nomElement le nom unique identifiant l'élément
	 * @return la valeur actuelle de l'élément.
	 * @since 1.0
	 */
	public String getValeur(String nomElement) {
		return verifierSiElementExiste(nomElement).getValeur();
	}

	/**
	 * Modifie la valeur de l'élément spécifié.
	 * 
	 * @param nomElement le nom unique identifiant l'élément
	 * @param valeur     la nouvelle valeur
	 * @since 1.0
	 */
	public void setValeur(String nomElement, String texte) {
		verifierSiElementExiste(nomElement).setValeur(texte);
	}

	private ElementFenetre verifierSiElementExiste(String nomElement) {
		ElementFenetre element = elementsParNom.get(nomElement);
		if (element == null) {
			afficherStackTrace("L'element \"" + nomElement + "\" n'existe pas !");
		}
		return element;
	}

	/**
	 * Efface toutes les images actuellement dessinées dans la fenêtre.
	 * 
	 * @since 1.0
	 */
	public void effacerImages() {
		panneau.effacerImages();
	}

	/**
	 * Fournit une image qui sera dessinée à la position spécifiée au sein de la
	 * fenêtre une fois la fonction {@link #dessinerImages} exécutée.
	 * 
	 * @param cheminImage le chemin d'accès à l'image (par exemple : "img\logo.png")
	 * @param x           une position horizontale supérieure ou égale à 0 exprimée
	 *                    en pixels (0 étant la position du bord gauche de la
	 *                    fenêtre)
	 * @param y           une position verticale supérieure ou égale à 0 exprimée en
	 *                    pixels (0 étant la position du bord supérieur de la
	 *                    fenêtre et l'axe étant orienté de haut en bas)
	 * @since 1.0
	 */
	public void preparerImage(String cheminImage, int x, int y) {
		preparerImage(cheminImage, x, y, -1, -1);
	}

	/**
	 * Fournit une image qui sera dessinée à la position spécifiée au sein de la
	 * fenêtre une fois la fonction {@link #dessinerImages} exécutée.
	 * 
	 * @param cheminImage le chemin d'accès à l'image (par exemple : "img\logo.png")
	 * @param x           une position horizontale supérieure ou égale à 0 exprimée
	 *                    en pixels (0 étant la position du bord gauche de la
	 *                    fenêtre)
	 * @param y           une position verticale supérieure ou égale à 0 exprimée en
	 *                    pixels (0 étant la position du bord supérieur de la
	 *                    fenêtre et l'axe étant orienté de haut en bas)
	 * @param largeur     la largeur exprimée en pixels que doit avoir l'image à
	 *                    l'affichage
	 * @param hauteur     la hauteur exprimée en pixels que doit avoir l'image à
	 *                    l'affichage
	 * @since 1.4
	 */
	public void preparerImage(String cheminImage, int x, int y, int largeur, int hauteur) {
		Dimension dimensions = null;
		if (largeur > 0 && hauteur > 0) {
			dimensions = new Dimension(largeur, hauteur);
		}
		panneau.preparerImage(getImage(cheminImage), new Position(y, x), dimensions);
	}

	private Image getImage(String cheminImage) {
		Image image = null;
		try {
			image = cheminsEtImages.get(cheminImage);
			if (image == null) {
				image = ImageIO.read(new File(cheminImage));
				cheminsEtImages.put(cheminImage, image);
			}
		} catch (IOException e) {
			afficherStackTrace("Le fichier \"" + cheminImage + "\" n'existe pas !");
		}
		return image;
	}

	/**
	 * Dessine toutes les images fournies préalablement à l'aide de la fonction
	 * {@link #preparerImage}.
	 * 
	 * @since 1.0
	 */
	public void dessinerImages() {
		panneau.dessinerImages();
	}

	/**
	 * Dessine une ligne reliant les deux points spécifiés.
	 * 
	 * @param x1 la coordonnée x du premier point
	 * @param y1 la coordonnée y du premier point
	 * @param x2 la coordonnée x du deuxième point
	 * @param y2 la coordonnée y du deuxième point
	 * 
	 * @since 1.6
	 */
	public void dessinerLigne(int x1, int y1, int x2, int y2) {
		panneau.dessinerLigne(Color.BLACK, new Position(y1, x1), new Position(y2, x2));
	}

	/**
	 * Dessine une ligne de couleur reliant les deux points spécifiés.
	 * 
	 * @param couleur la couleur de la ligne
	 * @param x1      la coordonnée x du premier point
	 * @param y1      la coordonnée y du premier point
	 * @param x2      la coordonnée x du deuxième point
	 * @param y2      la coordonnée y du deuxième point
	 * 
	 * @since 1.6
	 */
	public void dessinerLigne(Color couleur, int x1, int y1, int x2, int y2) {
		panneau.dessinerLigne(couleur, new Position(y1, x1), new Position(y2, x2));
	}

	/**
	 * Dessine un rectangle noir spécifié par une position, une largeur et une hauteur.
	 * 
	 * @param x       la coordonnée x de l'angle supérieur gauche du rectangle
	 * @param y       la coordonnée y de l'angle supérieur gauche du rectangle
	 * @param largeur la largeur du rectangle
	 * @param hauteur la hauteur du rectangle
	 * 
	 * @since 1.6
	 */
	public void dessinerRectangle(int x, int y, int largeur, int hauteur) {
		panneau.dessinerRectangle(Color.BLACK, new Position(y, x), largeur, hauteur);
	}

	/**
	 * Dessine un rectangle de couleur spécifié par une position, une largeur et une hauteur.
	 * 
	 * @param couleur la couleur du rectangle
	 * @param x       la coordonnée x de l'angle supérieur gauche du rectangle
	 * @param y       la coordonnée y de l'angle supérieur gauche du rectangle
	 * @param largeur la largeur du rectangle
	 * @param hauteur la hauteur du rectangle
	 * 
	 * @since 1.6
	 */
	public void dessinerRectangle(Color couleur, int x, int y, int largeur, int hauteur) {
		panneau.dessinerRectangle(couleur, new Position(y, x), largeur, hauteur);
	}

	@Override
	public void actionPerformed(ActionEvent evenement) {
		String nomElement = getElement((Component) evenement.getSource());
		signalerEvenement(nomElement, ActionFenetre.CLIC_BOUTON, getValeur(nomElement));
	}

	private String getElement(Component composant) {
		for (ElementFenetre element : elements) {
			if (element.possedeComposant(composant)) {
				return element.getNom();
			}
		}
		return null;
	}

	private void signalerEvenement(String nomElement, ActionFenetre action, String valeur) {
		if (auditeur != null) {
			auditeur.executerAction(this, nomElement, action, valeur);
		}
	}

	/**
	 * Ferme la fenêtre.
	 * 
	 * @since 1.5
	 */
	public void fermer() {
		cadre.dispatchEvent(new WindowEvent(cadre, WindowEvent.WINDOW_CLOSING));
	}

	private void afficherStackTrace(String message) {
		System.err.println(message);
		StackTraceElement[] elements = Thread.currentThread().getStackTrace();
		int nbLignes = Math.min(5, elements.length - 2);
		for (int i = 0; i < nbLignes; i++) {
			System.err.println("\t" + elements[2 + i]);
		}
		fermer();
	}

	private class Position {

		public int i, j;

		public Position(int i, int j) {
			this.i = i;
			this.j = j;
		}

		@Override
		public int hashCode() {
			return Objects.hash(i, j);
		}

		@Override
		public boolean equals(Object autre) {
			if (autre == this) {
				return true;
			}
			if (!(autre instanceof Position)) {
				return false;
			}
			Position position = (Position) autre;
			return i == position.i && j == position.j;
		}

		@Override
		public String toString() {
			return "[ligne = " + i + ", colonne = " + j + "]";
		}

	}

	private class Panneau extends JPanel {

		private static final long serialVersionUID = -2259378431692527399L;

		private List<ImageAffichage> imagesEtPositions = new LinkedList<ImageAffichage>();
		private CopyOnWriteArrayList<ImageAffichage> copieImagesEtPositions = new CopyOnWriteArrayList<ImageAffichage>();

		private List<Ligne> lignes = new LinkedList<Ligne>();
		private CopyOnWriteArrayList<Ligne> copieLignes = new CopyOnWriteArrayList<Ligne>();

		private List<Rectangle> rectangles = new LinkedList<Rectangle>();
		private CopyOnWriteArrayList<Rectangle> copieRectangles = new CopyOnWriteArrayList<Rectangle>();

		public void effacerImages() {
			imagesEtPositions.clear();
			lignes.clear();
			rectangles.clear();
		}

		public void preparerImage(Image image, Position position, Dimension dimensions) {
			imagesEtPositions.add(new ImageAffichage(image, position, dimensions));
		}

		public void dessinerLigne(Color couleur, Position p1, Position p2) {
			lignes.add(new Ligne(couleur, p1, p2));
			copieLignes = new CopyOnWriteArrayList<>(lignes);
			repaint();
		}

		public void dessinerRectangle(Color couleur, Position p, int largeur, int hauteur) {
			rectangles.add(new Rectangle(couleur, p, largeur, hauteur));
			copieRectangles = new CopyOnWriteArrayList<>(rectangles);
			repaint();
		}

		public void dessinerImages() {
			copieImagesEtPositions = new CopyOnWriteArrayList<>(imagesEtPositions);
			repaint();
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);

			for (ImageAffichage imageAffichage : copieImagesEtPositions) {
				if (imageAffichage.dimensions == null) {
					g.drawImage(imageAffichage.image, imageAffichage.position.j, imageAffichage.position.i, this);
				} else {
					g.drawImage(imageAffichage.image, imageAffichage.position.j, imageAffichage.position.i,
						imageAffichage.dimensions.width, imageAffichage.dimensions.height, this);
				}
			}

			Graphics2D g2d = (Graphics2D) g;
			/* Added by myself : Antialiasing for every object. Better final render. */
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			for (Ligne ligne : copieLignes) {
				g2d.setColor(ligne.couleur);
				g2d.drawLine(ligne.p1.j, ligne.p1.i, ligne.p2.j, ligne.p2.i);
			}

			for (Rectangle rectangle : copieRectangles) {
				g2d.setColor(rectangle.couleur);
				g2d.fillRect(rectangle.p.j, rectangle.p.i, rectangle.largeur, rectangle.hauteur);
			}
		}

		private class ImageAffichage {

			public Image image;
			public Position position;
			public Dimension dimensions;

			public ImageAffichage(Image image, Position position, Dimension dimensions) {
				this.image = image;
				this.position = position;
				this.dimensions = dimensions;
			}

		}

		private class Ligne {

			public Color couleur;
			public Position p1, p2;

			public Ligne(Color couleur, Position p1, Position p2) {
				this.couleur = couleur;
				this.p1 = p1;
				this.p2 = p2;
			}

		}

		private class Rectangle {

			public Color couleur;
			public Position p;
			public int largeur, hauteur;

			public Rectangle(Color couleur, Position p, int largeur, int hauteur) {
				this.couleur = couleur;
				this.p = p;
				this.largeur = largeur;
				this.hauteur = hauteur;
			}

		}

	}

}

abstract class ElementFenetre {

	private String nom;
	private Object composant;

	public ElementFenetre(String nom, Object composant) {
		this.nom = nom;
		this.composant = composant;
	}

	public String getNom() {
		return nom;
	}

	abstract String getValeur();

	abstract void setValeur(String valeur);

	abstract void positionner(int x, int y, int largeur, int hauteur);

	abstract void setPolice(Font police);

	abstract void setCouleurTexte(Color couleur);

	public Object getComposant() {
		return composant;
	}

	public boolean possedeComposant(Object composant) {
		return this.composant.equals(composant);
	}

	@Override
	public int hashCode() {
		return nom.hashCode();
	}

	@Override
	public boolean equals(Object autre) {
		if (this == autre) {
			return true;
		}
		if (!(autre instanceof ElementFenetre)) {
			return false;
		}
		ElementFenetre element = (ElementFenetre) autre;
		return nom.equals(element.nom);
	}
}

abstract class ElementVisible extends ElementFenetre {

	public ElementVisible(String nom, Component composant) {
		super(nom, composant);
	}

	abstract String getValeur();

	abstract void setValeur(String valeur);

	public void positionner(int x, int y, int largeur, int hauteur) {
		((Component) getComposant()).setBounds(x, y, largeur, hauteur);
	}

	public void setPolice(Font police) {
		((Component) getComposant()).setFont(police);
	}

	public void setCouleurTexte(Color couleur) {
		((Component) getComposant()).setForeground(couleur);
	}

}

class Etiquette extends ElementVisible {

	public Etiquette(String nom, JLabel etiquette) {
		super(nom, etiquette);
	}

	@Override
	public String getValeur() {
		return ((JLabel) getComposant()).getText();
	}

	@Override
	public void setValeur(String valeur) {
		((JLabel) getComposant()).setText(valeur);
	}

}

class ZoneSaisie extends ElementVisible {

	public ZoneSaisie(String nom, JTextField zoneSaisie) {
		super(nom, zoneSaisie);
	}

	@Override
	public String getValeur() {
		return ((JTextField) getComposant()).getText();
	}

	@Override
	public void setValeur(String valeur) {
		((JTextField) getComposant()).setText(valeur);
	}

}

class ListeDeroulante extends ElementVisible {

	public ListeDeroulante(String nom, JComboBox<String> listeDeroulante, String valeurs) {
		super(nom, listeDeroulante);
		for (String option : valeurs.split("\n")) {
			listeDeroulante.addItem(option);
		}
	}

	@Override
	public String getValeur() {
		@SuppressWarnings("unchecked")
		JComboBox<String> listeDeroulante = (JComboBox<String>) getComposant();
		return String.valueOf(listeDeroulante.getSelectedItem());
	}

	@Override
	public void setValeur(String valeur) {
		@SuppressWarnings("unchecked")
		JComboBox<String> listeDeroulante = (JComboBox<String>) getComposant();
		listeDeroulante.setSelectedItem(valeur);
	}

}

class Bouton extends ElementVisible {

	public Bouton(String nom, JButton bouton) {
		super(nom, bouton);
	}

	@Override
	public String getValeur() {
		return ((JButton) getComposant()).getText();
	}

	@Override
	public void setValeur(String valeur) {
		((JButton) getComposant()).setText(valeur);
	}

}

class CaseCocher extends ElementVisible {

	public CaseCocher(String nom, JCheckBox caseCocher) {
		super(nom, caseCocher);
	}

	@Override
	public String getValeur() {
		return String.valueOf(((JCheckBox) getComposant()).isSelected());
	}

	@Override
	public void setValeur(String valeur) {
		((JCheckBox) getComposant()).setSelected(valeur.equals("true") ? true : false);
	}

}

class GroupeBoutons extends ElementFenetre {

	private Set<BoutonRadio> boutons = new HashSet<BoutonRadio>();

	public GroupeBoutons(String nom, ButtonGroup groupeBoutons) {
		super(nom, groupeBoutons);
	}

	public void ajouter(BoutonRadio bouton) {
		if (boutons.add(bouton)) {
			((ButtonGroup) getComposant()).add((JRadioButton) bouton.getComposant());
		}
	}

	@Override
	public String getValeur() {
		for (BoutonRadio boutonRadio : boutons) {
			JRadioButton bouton = (JRadioButton) boutonRadio.getComposant();
			if (bouton.isSelected()) {
				return boutonRadio.getNom();
			}
		}
		return null;
	}

	@Override
	public void setValeur(String nomElement) {
		for (BoutonRadio boutonRadio : boutons) {
			if (boutonRadio.getNom().equals(nomElement)) {
				((JRadioButton) boutonRadio.getComposant()).setSelected(true);
			}
		}
	}

	@Override
	void positionner(int x, int y, int largeur, int hauteur) {
		// Ne fait rien
	}

	@Override
	void setPolice(Font police) {
		for (BoutonRadio boutonRadio : boutons) {
			boutonRadio.setPolice(police);
		}
	}

	@Override
	void setCouleurTexte(Color couleur) {
		for (BoutonRadio boutonRadio : boutons) {
			boutonRadio.setCouleurTexte(couleur);
		}
	}

}

class BoutonRadio extends ElementVisible {

	public BoutonRadio(String nom, JRadioButton boutonRadio) {
		super(nom, boutonRadio);
	}

	@Override
	public String getValeur() {
		return String.valueOf(((JRadioButton) getComposant()).isSelected());
	}

	@Override
	public void setValeur(String valeur) {
		((JRadioButton) getComposant()).setSelected(valeur.equals("true") ? true : false);
	}

}

final class Theme {

	static {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Dimensions
	public static final String TITRE = "Sans nom";
	public static final int LARGEUR = 640;
	public static final int HAUTEUR = 480;
	public static final int NB_COLONNES = 5;
	public static final int NB_LIGNES = 10;

	// Police de caractères
	public static final String NOM_POLICE = "Arial";
	public static final int TAILLE_POLICE = 13;

	// Couleurs
	public static final Color COULEUR_TEXTE = Color.BLACK;
	public static final Color COULEUR_ARRIERE_PLAN = new Color(240, 240, 240);

}