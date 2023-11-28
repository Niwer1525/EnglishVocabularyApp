package io;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

/**
 * This class represents a dictionary of words.
 * The dictionary is stored in a external file.
 * 
 * Inpired by the class Dictionnaire of the course "Programmation de Base" by M. Comblin.
 * 
 * I've changed it to make it Oriented Object. That's allow to make multiple dictionaries.
 * @author Erwin Redoté
 * @author Arnaud Comblin
 */
public class Dictionary {

	private final List<String> WORDS;

	/**
	 * Create a new dictionary from the specified file.
	 * @param filePath the path of the file (relative to the project, e.g : "data/vocabulary.txt")
	 */
	public Dictionary(String filePath) {
		WORDS = readLines(filePath, StandardCharsets.UTF_8);
	}

	/**
	 * Return the word at the specified line.
	 * @param line the line number
	 * @return the word at the specified line
	 */
	public String getWord(int line) {
		String word = "Unknown,Inconnu";
		if (line >= 0 && line < size()) word = WORDS.get(line);
		return word;
	}
	
	public String[] getWords(int line) {
		return getWord(line).split(", ");
	}

	/**
	 * Return the size of the dictionary.
	 * @return
	 */
	public int size() {
		return WORDS.size();
	}
	
	private static List<String> readLines(String filePath, Charset encoding) {
		List<String> lines;
		Path path = Path.of(System.getProperty("user.dir")+"/"+filePath);
	    try {
			lines = Files.readAllLines(path, encoding);
		} catch (IOException e) {
			lines = new LinkedList<String>();
			System.err.printf("Error during file reading (%s). Creating empty list.\n", filePath);
			e.printStackTrace();
		}
	    return lines;
	}
}
