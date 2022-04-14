import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Hashtable;

/**
 * The StopList class creates a hash table containing all the stop words, which can be retrieved for other uses.
 * @author Alex Perinetti
 *
 */

public class StopList {
    private Hashtable<Integer, String> stopList;
    
    /**
     * The constructor, adds a preset list of stop words to the hash table.
     */
    public StopList () {
        stopList = new Hashtable<Integer, String>();
        String words[] = {"a",
                          "about",
                          "above",
                          "after",
                          "again",
                          "against",
                          "all",
                          "am",
                          "an",
                          "and",
                          "any",
                          "are",
                          "aren't",
                          "as",
                          "at",
                          "be",
                          "because",
                          "been",
                          "before",
                          "being",
                          "below",
                          "between",
                          "both",
                          "but",
                          "by",
                          "can't",
                          "cannot",
                          "could",
                          "couldn't",
                          "did",
                          "didn't",
                          "do",
                          "does",
                          "doesn't",
                          "doing",
                          "don't",
                          "down",
                          "during",
                          "each",
                          "few",
                          "for",
                          "from",
                          "further",
                          "had",
                          "hadn't",
                          "has",
                          "hasn't",
                          "have",
                          "haven't",
                          "having",
                          "he",
                          "he'd",
                          "he'll",
                          "he's",
                          "her",
                          "here",
                          "here's",
                          "hers",
                          "herself",
                          "him",
                          "himself",
                          "his",
                          "how",
                          "how's",
                          "i",
                          "i'd",
                          "i'll",
                          "i'm",
                          "i've",
                          "if",
                          "in",
                          "into",
                          "is",
                          "isn't",
                          "it",
                          "it's",
                          "its",
                          "itself",
                          "let's",
                          "me",
                          "more",
                          "most",
                          "mustn't",
                          "my",
                          "myself",
                          "no",
                          "nor",
                          "not",
                          "of",
                          "off",
                          "on",
                          "once",
                          "only",
                          "or",
                          "other",
                          "ought",
                          "our",
                          "ours",
                          "ourselves",
                          "out",
                          "over",
                          "own",
                          "same",
                          "shan't",
                          "she",
                          "she'd",
                          "she'll",
                          "she's",
                          "should",
                          "shouldn't",
                          "so",
                          "some",
                          "such",
                          "than",
                          "that",
                          "that's",
                          "the",
                          "their",
                          "theirs",
                          "them",
                          "themselves",
                          "then",
                          "there",
                          "there's",
                          "these",
                          "they",
                          "they'd",
                          "they'll",
                          "they're",
                          "they've",
                          "this",
                          "those",
                          "through",
                          "to",
                          "too",
                          "under",
                          "until",
                          "up",
                          "very",
                          "was",
                          "wasn't",
                          "we",
                          "we'd",
                          "we'll",
                          "we're",
                          "we've",
                          "were",
                          "weren't",
                          "what",
                          "what's",
                          "when",
                          "when's",
                          "where",
                          "where's",
                          "which",
                          "while",
                          "who",
                          "who's",
                          "whom",
                          "why",
                          "why's",
                          "with",
                          "won't",
                          "would",
                          "wouldn't",
                          "you",
                          "you'd",
                          "you'll",
                          "you're",
                          "you've",
                          "your",
                          "yours",
                          "yourself",
                          "yourselves"};
        //Adds each element of the stop word array to the stopList
        for (int i = 0; i < words.length; i++) {
            stopList.put(i, words[i]);
        }
    }
    
    /**
     * Returns the stop list.
     * @return Hashtable<Integer, String> The stopList that was created in the constructor
     */
    public Hashtable<Integer, String> getStopList() {
        return stopList;
    }
    
    /**
     * Outputs the contents of the stop list to outputStopList.txt.
     */
    public void outputList() {
        PrintWriter file;
        try {
            file = new PrintWriter("outputStopList.txt");
            file.println(stopList.toString());
            file.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
