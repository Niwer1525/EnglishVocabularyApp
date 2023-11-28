package main;

import java.util.Arrays;

import io.ActionFenetre;
import io.AlignementTexte;
import io.Auditeur;
import io.Dictionary;
import io.Fenetre;

public class App implements Auditeur {

    private static final int WIDTH = 800, HEIGHT = 600;
    private static final int MAX_WORDS = 10;
    private static int currentWordCount = 1;
    private static String[] currentWord = new String[2];
    private Dictionary dictionary;
    private int points = 0;
    
    public App() {
        this.dictionary = new Dictionary("data/vocabulaire.txt");
    }

    public static void main(String[] args) {
        Fenetre window = new Fenetre("", WIDTH, HEIGHT, 1, 5);
        App vocabulary = new App();

        vocabulary.init();

        window.setAuditeur(vocabulary);
        window.ajouterEtiquette("currentWord", "Question n° "+currentWordCount, AlignementTexte.CENTRE, 0, 0);
        window.ajouterEtiquette("wordToTranslate", vocabulary.getSentence(), AlignementTexte.CENTRE, 1, 0);
        window.ajouterZoneSaisie("word", "", 2, 0);
        window.ajouterEtiquette("prevResult", "", AlignementTexte.CENTRE, 3, 0);
        window.ajouterBouton("buttonOk", "Vérifier", 4, 0);
        
        window.afficher();
    }

    /**
     * Init the game
     */
    private void init() {
       currentWord = generateWord();
    }

    /**
     * Terminate the game
     * @param instance The window
     */
    private void endGame(Fenetre instance) {
        instance.setValeur("wordToTranslate", "Vous avez " + points + "/" + MAX_WORDS + " !");
        instance.setValeur("word", "");
    }

    @Override
    public void executerAction(Fenetre instance, String elementNane, ActionFenetre action, String value) {
        if(action == ActionFenetre.PRESSION_TOUCHE && value.equalsIgnoreCase("enter")) {
            if(currentWordCount > MAX_WORDS) endGame(instance);
            else checkWord(instance, instance.getValeur("word"));
            return;
        }
        switch(elementNane) {
        case "buttonOk":
            checkWord(instance, instance.getValeur("word"));
            if(currentWordCount > MAX_WORDS) endGame(instance);
            break;
        }
    }

    /**
     * Check if the word is correct or not
     * @param word The word to check
     */
    private void checkWord(Fenetre instance, String word) {
        boolean hasNoError = word.equalsIgnoreCase(currentWord[1]);
        if(hasNoError) this.points++;
        
        /* Show the error to the user (If there is one or more error(s)) */
        instance.setValeur("prevResult", "Dernière réponse : "+word
            +" (" + (hasNoError ? "Correcte" : "Incorrecte") + ") "
            +(hasNoError ? "" : currentWord[0]+" -> : "+currentWord[1])
        );

        /* Generate a new word */
        currentWord = generateWord();

        /* Reset state */
        instance.setValeur("currentWord", "Question n° " + currentWordCount++);
        instance.setValeur("word", "");
        instance.setValeur("wordToTranslate", getSentence());
    }

    /**
     * Generate randomly a word contained in the current dictionary
     * @return The randomly generated word
     */
    private String[] generateWord() {
        String[] words = dictionary.getWords(randomize(0, dictionary.size() - 1));
        return randomize(0, 2) == 1 ? combine(swap(words), "ANGLAIS") : combine(words, "FRANCAIS");
    }

    /**
     * Swap the two arguments of the array
     * @param words The array to swap
     * @return The swaped array
     */
    private String[] swap(String[] words) {
        String tmp = words[0];
        words[0] = words[1];
        words[1] = tmp;
        return words;
    }

    /**
     * Generate a random number between min and max
     * @param min The minimum number
     * @param max The maximum number
     * @return The randomly generated number
     */
    private int randomize(int min, int max) {
        return (int) (Math.random() * (max - min)) + min;
    }

    /**
     * 
     * @param a The array of words
     * @param lang The language the user will translate to
     * @return An array containing 3 elements '0' the word to translate '1' the translation (For results checking) and '2' 
     * the language in wich the user will translate the word.
     */
    private String[] combine(String[] a, String lang) {
        String[] copy = Arrays.copyOf(a, 3);
        copy[2] = lang;
        return copy;
    }

    private String getSentence() {
        return String.format("Traduisez en %s : %s", currentWord[2], currentWord[0]);
    }
}   