import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

/**
 * The InvertedIndex class creates the index and allows for searches to be made on it.
 * @author Alex Perinetti
 *
 */

public class InvertedIndex {
    private String dir;
    private File[] corpus;
    private Hashtable<String, LinkedList<Map.Entry<File, Integer>>> index;
    private Hashtable<Integer, String> stopList;
    private Hashtable<String, LinkedList<Map.Entry<File, Integer>>> stemmedIndex;
    
    /**
     * Constructor that stores the directory path, gets the file list and stop list, and makes the index.
     * @param dir The path of the folder containing the files that will be indexed
     */
    public InvertedIndex(String dir) {
        this.dir = dir;
        corpus = new File(dir).listFiles();
        stopList = new StopList().getStopList();
        makeIndex();
    }
    
    /**
     * Creates a the Inverted Index in a Hashtable structure that takes in the word and a list containing
     * the file and location of each occurrence of the word in the corpus.
     */
    private void makeIndex() {
        if (readIndexFromStorage(false)) {
            return;
        }
        
        index = new Hashtable<String, LinkedList<Map.Entry<File, Integer>>>();
        
        //Creates a scanner for each file in the corpus directory
        for (int i = 0; i < corpus.length; i++) {
            Scanner sc = null;
            try {
                sc = new Scanner(corpus[i]);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            //Goes through each word in the file, getting rid of punctuation and numbers, and converting to lower
            //case before adding to the inverted index
            int j = 0;
            while(sc.hasNext()) {
                String word = sc.next();
                word = word.replaceAll("\\p{Punct}", "");
                word = word.replaceAll("\\d", "");
                word = word.toLowerCase();
                if (!stopList.contains(word) && !word.equals("")) {
                    if (index.containsKey(word)) {
                        LinkedList<Map.Entry<File, Integer>> list = index.get(word);
                        list.add(new AbstractMap.SimpleEntry<>(corpus[i], j));
                        index.replace(word, list);
                    } else {
                        LinkedList<Map.Entry<File, Integer>> list = new LinkedList<>();
                        list.add(new AbstractMap.SimpleEntry<>(corpus[i], j));
                        index.put(word, list);
                    }
                }
                j++;
            }
        }
        
        //Output the index hash table to a file
        writeIndexForStorage(false);
    }
    
    /**
     * After the inverted index is created, Porter's Algorithm in the Stemmer class is used to make an
     * inverted index that contains only the stemmed words.
     */
    public void makeStemmedIndex () {
        if (readIndexFromStorage(true))
            return;
        
        stemmedIndex = new Hashtable<>();
        
        index.forEach((k, v) -> {
            Stemmer s = new Stemmer();
            for (int i = 0; i < k.length(); i++) {
                s.add(k.charAt(i));
            }
            s.stem();
            String word = s.toString();
            if (stemmedIndex.containsKey(word)) {
                //If two words are stemmed to the same root, then their lists of files and locations are combined
                LinkedList<Map.Entry<File, Integer>> list1 = index.get(k);
                LinkedList<Map.Entry<File, Integer>> list2 = stemmedIndex.get(word);
                for (int j = 0; j < list1.size(); j++)
                    list2.add(list1.get(j));
                stemmedIndex.replace(word, list2);
            } else {
                //Otherwise the list of the unstemmed word is added to the new index with the stemmed word
                LinkedList<Map.Entry<File, Integer>> list = index.get(k);
                stemmedIndex.put(word, list);
            }
        });
        
        //Write the stemmed index to a file
        writeIndexForStorage(true);
    }
    
    /**
     * Outputs the entire index to a txt file called outputInvertedIndex.txt
     */
    public void outputIndex(boolean useStemmed) {
        PrintWriter writer;
        Hashtable<String, LinkedList<Map.Entry<File, Integer>>> index;
        if (useStemmed)
            index = stemmedIndex;
        else
            index = this.index;
        
        try {
            writer = new PrintWriter("outputInvertedIndex.txt");
            //For each word in the index, the word is printed with the information in its linked list
            index.forEach((k, v) -> {
                writer.print(k + ": {");
                v.forEach(entry -> {
                   writer.print("(" + entry.getKey().getName() + ", " + entry.getValue() + "), "); 
                });
                writer.print("}");
                writer.println();
            });
            writer.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    /**
     * General method for searching for words, used by searchWord and searchWordStemmed
     * @param word The word that is to be searched for
     * @param useStemmed Indicates which index to use, true - stemmed index, false - inverted index
     * @return Hashtable<File, Integer> Contains the files the word is found in and how many times in each file
     */
    private Hashtable<File, Integer> search (String word, boolean useStemmed) {
        LinkedList<Map.Entry<File, Integer>> list;
        Hashtable<File, Integer> visited = new Hashtable<>();
        
        if (!useStemmed) {
            list = index.get(word.toLowerCase());
        } else {
            list = stemmedIndex.get(word.toLowerCase());
        }
        
        if (list == null)
            return null;
        
        list.forEach(entry -> {
            File doc = entry.getKey();
            if(visited.containsKey(doc)) {
                int count = visited.get(doc);
                count++;
                visited.replace(doc, count);
            } else {
                visited.put(doc, 1);
            }
        });
        return visited;
    }
    
    /**
     * The word is found in the index, and then the number of occurrences in each document is counted and the 
     * results are written to the output file path
     * @param w The word to be searched for in the index
     * @param output The path of the file where the output will be written
     * @param graphic Indicates what type of output is used, 0=txt file, 1=gui, 2=both
     * @throws FileNotFoundException If the path of the output file in invalid
     */
    public void searchWord(String w, String output, int length, int graphic) throws FileNotFoundException {
        //The word is formatted the same way terms were when being placed in the inverted index
        String temp = w.replaceAll("\\p{Punct}", "");
        temp = temp.replaceAll("\\d", "");
        temp = temp.toLowerCase();
        String word = temp;
        
        PrintWriter writer = new PrintWriter(output);
        Hashtable<File, Integer> visited = search(word, false);
        
        if (visited == null) {
            writer.println("No results found for " + w);
            writer.close();
        } else {
            //Outputting the results
            if (graphic == 0 || graphic == 2) {
                writer.println("Search Results for the word: " + w);
                visited.forEach((k, v) -> {
                    writer.println(k.getName() + ": " + v);
                    //Add snippet
                    try {
                        writer.println("\t\"..." + snippet(length, word, k, false) + "...\"");
                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        writer.println();
                    }
                });
            }
            writer.close();
        }
        
        //display on gui
        if (graphic == 1 || graphic == 2) {
            displayWord(word, visited, length);
        }
    }
    
    /**
     * The search for the original word stays the same. If the stemmed word is different, then another search
     * is made to the stemmed index to get more results that may be relevant to the query.
     * @param word The word to be searched for and stemmed
     * @param output The path of the file where the output will be written
     * @param graphic Indicates what type of output is used, 0=txt file, 1=gui, 2=both
     * @throws FileNotFoundException If the path of the output file is invalid
     */
    public void searchWordStemmed(String w, String output, int length, int graphic) throws FileNotFoundException {
        //The word is formatted the same way terms were when being placed in the inverted index
        String temp = w.replaceAll("\\p{Punct}", "");
        temp = temp.replaceAll("\\d", "");
        temp = temp.toLowerCase();
        String word = temp;
        
        PrintWriter writer = new PrintWriter(output);
        Hashtable<File, Integer> visited = search(word, false);
        Hashtable<File, Integer> visitedStemmed = null;
        Stemmer stem = new Stemmer();
        
        if (graphic == 0 || graphic ==2) {
            if(visited == null) {
                writer.println("No results found for " + w);
            } else {
                //Outputting the results
                writer.println("Search Results for the word: " + w);
                visited.forEach((k, v) -> {
                    writer.println(k.getName() + ": " + v);
                    //Add snippet
                    try {
                        writer.println("\t\"..." + snippet(length, word, k, false) + "...\"");
                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        writer.println();
                    }
                });
            }
        }
        
        //Get the stemmed word, and if it is different get the search from the stemmed index
        for (int i = 0; i < word.length(); i++) {
            stem.add(word.charAt(i));
        }
        stem.stem();
        String stemmedWord = stem.toString();
        
        if (!stemmedWord.equals(word)) {
            visitedStemmed = search(stemmedWord, true);
            if (graphic == 0 || graphic == 2) {
                writer.println();
                if(visitedStemmed == null) {    
                    writer.println("No result found for " + stemmedWord);
                    writer.close();
                    return;
                }
                writer.println("Search Results for similar word: " + stemmedWord);
                visitedStemmed.forEach((k, v) -> {
                    writer.println(k.getName() + ": " + v);
                    try {
                        writer.println("\t\"..." + snippet(length, stemmedWord, k, true) + "...\"");
                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        writer.println();
                    }
                });
            }
        }
        writer.close();
        
        //display on gui
        if (graphic == 1 || graphic == 2)
            displayWordStemmed(word, stemmedWord, visited, visitedStemmed, length);
    }
    
    /**
     * Searches the entire index for occurrences of the given document, and outputs which words have the 
     * document in their list, and how many times the word appears in the document.
     * @param doc The name of the document to be search for in the index
     * @param output The path of the file where the results will be output to
     * @throws FileNotFoundException If the output path is invalid
     */
    public void searchDoc(String doc, String output) throws FileNotFoundException {
        PrintWriter writer = new PrintWriter(output);
        
        //Checks if the document is in the file list of the corpus
        int i;
        for(i = 0; i < corpus.length; i++) {
            if (corpus[i].getName().equals(doc))
                break;
        }
        if (i == corpus.length) {
            writer.println("No results found for the document: " + doc);
            writer.close();
            return;
        }
        
        //Goes through each word's list in the index, and counts how many times the document appears in the list
        //This is the word's count which will be output to the output file
        writer.println("Search Results for the document: " + doc);
        index.forEach((k, v) -> {
            int count = 0;
            for(int j = 0; j < v.size(); j++) {
                if (v.get(j).getKey().getName().equals(doc)) {
                    count ++;
                }
            }
            if (count > 0) {
                writer.println(k + ": " + count);
            }
        });
        writer.close();
    }
    
    /**
     * All information in the index on the given word will be output to the given file path.
     * @param word The word that will have its information printed
     * @param output The file path that the information will be output to
     * @throws FileNotFoundException If the output file path is invalid
     */
    public void printWord(String word, String output) throws FileNotFoundException {
        PrintWriter writer = new PrintWriter(output);
        
        //If the index does not have the given word, there is nothing to print
        if(!index.containsKey(word)) {
            writer.println("Word " + word + " not found in the Inverted Index");
            writer.close();
            return;
        }
        
        //The list of the given word is formatted to the output, each entry on a line
        writer.println("Inverted Index contents for the word: " + word);
        writer.println("Format is filename:location; The location is the number of words from the beginning of the file");
        LinkedList<Map.Entry<File, Integer>> list = index.get(word);
        list.forEach(entry -> {
            writer.println(entry.getKey().getName() + ": " + entry.getValue());
        });
        
        writer.close();
    }
    
    /**
     * Prints all the words that are in the given document with their location to the given output file.
     * @param doc The document whose information is to be printed
     * @param output The path of the file where the information will be printed
     * @throws FileNotFoundException If the output file path is invalid
     */
    public void printDoc(String doc, String output) throws FileNotFoundException {
        PrintWriter writer = new PrintWriter(output);
        
        //Checks if the document is in the corpus
        int i;
        for(i = 0; i < corpus.length; i++) {
            if (corpus[i].getName().equals(doc))
                break;
        }
        if (i == corpus.length) {
            writer.println("Document " + doc + " not found in the Inverted Index");
            writer.close();
            return;
        }
        
        //Checks each word's list in the index, if the document is in it, outputs the word and the location on
        //a line of the output file
        writer.println("Inverted Index contents for the document: " + doc);
        writer.println("Format is word:location; The location is the number of words from the beginning of the file");
        index.forEach((k, v) -> {
            for(int j = 0; j < v.size(); j++) {
                if (v.get(j).getKey().getName().equals(doc)) {
                    writer.println(k + ": " + v.get(j).getValue());
                }
            }
        });
        
        writer.close();
    }
    
    /**
     * Writes the index object to a text file to be read in at a later time
     * @param useStemmed Indicates which index if being written, the stemmed or not stemmed
     */
    private void writeIndexForStorage(boolean useStemmed) {
        Hashtable<String, LinkedList<Map.Entry<File, Integer>>> index;
        String fileName;
        if (useStemmed) {
            index = stemmedIndex;
            fileName = "./Data/StemmedIndex.txt";
        }
        else {
            index = this.index;
            fileName = "./Data/InvertedIndex.txt";
        }
        
        File path = new File("./Data");
        if (!path.exists())
            path.mkdir();
        
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName));
            out.writeObject(index);
            out.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    /**
     * Reads in the index from a previously created text file. This file should have been made using the 
     * writeIndexToStorage method
     * @param useStemmed Indicates which index is being read, the stemmed or not stemmed
     * @return Returns true if the index was read in, false if not
     */
    @SuppressWarnings("unchecked") //The only files that would be in the data folder are ones created by the
    //program. 
    private boolean readIndexFromStorage(boolean useStemmed) {
        Hashtable<String, LinkedList<Map.Entry<File, Integer>>> temp;
        String fileName;
        if (useStemmed) 
            fileName = "./Data/StemmedIndex.txt";
        else
            fileName = "./Data/InvertedIndex.txt";
        
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName));
            temp = (Hashtable<String, LinkedList<Map.Entry<File, Integer>>>) in.readObject();
            in.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            temp = null;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            temp = null;
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            temp = null;
        }
        
        if (temp == null)
            return false;
        
        if (useStemmed)
            stemmedIndex = temp;
        else
            index = temp;
        return true;
    }
    
    /**
     * Finds and returns a snippet from a document of a word's first appearance.
     * @param length The number of words before and after the desired word
     * @param word The word that will be at the center of the snippet
     * @param doc The document where the snippet will be taken from
     * @param useStemmed Uses the stemmed index if true
     * @return String of the snippet
     * @throws FileNotFoundException If the doc's path is invalid
     */
    private String snippet(int length, String word, File doc, boolean useStemmed) throws FileNotFoundException {
        Scanner input = new Scanner(doc);
        LinkedList<Map.Entry<File, Integer>> locations;
        if(!useStemmed) {
            locations = index.get(word.toLowerCase());
        } else {
            locations = stemmedIndex.get(word.toLowerCase());
        }
        int location = 0;
        
        for(int i = 0; i < locations.size(); i++) {
            if (locations.get(i).getKey().equals(doc)) {
                location = locations.get(i).getValue();
                break;
            }
        }
        
        if (location == 0) {
            input.close();
            return null;
        }
            
        
        int start = location-length;
        int end = location+length;
        
        if (start <= 0)
            start = 1;
        
        String snippet = "";
        int count = 0;
        while(input.hasNext() && count <= end) {
            count++;
            String in = input.next();
            if(count == start)
                snippet += in;
            if (count > start && count <= end)
                snippet += " " + in;
        }
        
        input.close();
        return snippet;
    }
    
    /**
     * Searches for each word of the query and returns the documents that contain all the words that 
     * are not stop words.
     * @param query The set of words that will be searched in the corpus
     * @param output The path of the output file
     * @param graphic Indicates what type of output is used, 0=txt file, 1=gui, 2=both
     * @throws FileNotFoundException If the output path is invalid
     */
    public void searchWords(String query, String output, int length, int graphic) throws FileNotFoundException {
        PrintWriter writer = new PrintWriter(output);
        String[] w = query.split(" ");
        LinkedList<File> results = null;
        
        //Formats all the words in the same way the terms of the index were formatted
        LinkedList<String> tempList = new LinkedList<>();
        for (int i = 0; i < w.length; i ++) {
            String t = w[i].replaceAll("\\p{Punct}", "").replaceAll("\\d", "").toLowerCase();
            if (!t.equals("")) {
                tempList.add(t);
            }
        }
        String[] words = Arrays.stream(tempList.toArray()).toArray(String[]::new);
        
        for (int i = 0; i < words.length; i++) {
            if (!stopList.contains(words[i].toLowerCase())) {
                Hashtable<File, Integer> search = search(words[i], false);
                if (search == null) {
                    results = new LinkedList<>();
                    break;
                }
                if (results == null) {
                    LinkedList<File> temp = new LinkedList<>();
                    search.forEach((k, v) -> {
                        temp.add(k);
                    });
                    results = temp;
                } else {
                    LinkedList<File> resultsTemp = results;
                    LinkedList<File> temp = new LinkedList<>();
                    search.forEach((k, v) -> {
                        if (resultsTemp.contains(k))
                            temp.add(k);
                    });
                    results = temp;
                }
            }
        }
        
        if (graphic == 0 || graphic == 2) {
            if (results.isEmpty()) {
                writer.println("No results found for: " + query);
                writer.close();
            } else {
                writer.println("Search Results for: " + query);
                for (int i = 0; i < results.size(); i++) {
                    writer.println(results.get(i).getName());
                    for (int j = 0; j < words.length; j++) {
                        if (!stopList.contains(words[j].toLowerCase()))
                            writer.println("\t\"..." + snippet(length, words[j], results.get(i), false) + "...\"");
                    }
                }
                writer.close();
            }
        }
        
        if (graphic == 1 || graphic == 2) {
           //Display to gui
            displayWords(query, results, words, length); 
        }
    }
    
    /**
     * Searches each word of the query and prints the set of documents that contain all the words that are not 
     * stop words, and then does the same but for the stemmed words, printing any documents that may not have
     * appeared in the previous search 
     * @param query The set of words that will be searched in the corpus
     * @param output The path of the output file
     * @param graphic Indicates what type of output is used, 0=txt file, 1=gui, 2=both
     * @throws FileNotFoundException If the output path is invalid
     */
    public void searchWordsStemmed (String query, String output, int length, int graphic) throws FileNotFoundException {
        PrintWriter writer = new PrintWriter(output);
        String[] w = query.split(" ");
        LinkedList<File> results = null;
        LinkedList<File> stemResults = null;
        
        //Formats the words in the query
        LinkedList<String> tempList = new LinkedList<>();
        for (int i = 0; i < w.length; i ++) {
            String t = w[i].replaceAll("\\p{Punct}", "").replaceAll("\\d", "").toLowerCase();
            if (!t.equals("")) {
                tempList.add(t);
            }
        }
        String[] words = Arrays.stream(tempList.toArray()).toArray(String[]::new);
        String[] stemmedWords = new String[words.length];
        
        for (int i = 0; i < words.length; i++) {
            //gets file list for the unstemmed words
            if(!stopList.contains(words[i].toLowerCase())) {
                Hashtable<File, Integer> search = search(words[i], false);
                if (search == null) {
                    results = new LinkedList<>();
                    break;
                }
                if (results == null) {
                    LinkedList<File> temp = new LinkedList<>();
                    search.forEach((k, v) -> {
                        temp.add(k);
                    });
                    results = temp;
                } else {
                    LinkedList<File> resultsTemp = results;
                    LinkedList<File> temp = new LinkedList<>();
                    search.forEach((k, v) -> {
                        if (resultsTemp.contains(k))
                            temp.add(k);
                    });
                    results = temp;
                }  
            }
        }
        for(int i = 0; i < words.length; i++) {    
            //get file list for stemmed word
            Stemmer stem = new Stemmer();
            for (int j = 0; j < words[i].length(); j++) {
                stem.add(words[i].charAt(j));
            }
            stem.stem();
            String stemWord = stem.toString().toLowerCase();
            stemmedWords[i] = stemWord;
            if (!stopList.contains(stemWord)) {
                Hashtable<File, Integer> search = search(stemWord, true);
                if (search == null) {
                    stemResults = new LinkedList<>();
                    break;
                }
                else if (stemResults == null) {
                    LinkedList<File> temp = new LinkedList<>();
                    search.forEach((k, v) -> {
                        temp.add(k);
                    });
                    stemResults = temp;
                } else {
                    LinkedList<File> resultsTemp = results;
                    LinkedList<File> temp = new LinkedList<>();
                    search.forEach((k, v) -> {
                        if (resultsTemp.contains(k))
                            temp.add(k);
                    });
                    stemResults = temp;
                }
            }
        }
        
        if (graphic == 0 || graphic == 2) {
            //Print the unstemmed results first
            if (results.isEmpty()) {
                writer.println("No results found for: " + query);
            } else {
                writer.println("Search Results for: " + query);
                for (int i = 0; i < results.size(); i++) {
                    writer.println(results.get(i).getName());
                    for (int j = 0; j < words.length; j++) {
                        if (!stopList.contains(words[j].toLowerCase()))
                            writer.println("\t\"..." + snippet(length, words[j], results.get(i), false) + "...\"");
                    }
                }
            }
            writer.println();
            
            //Print stemmed results next
            if (stemResults.isEmpty()) {
                writer.println("No results found for similar words either");
                writer.close();
                return;
            }
        }
        
        //Gets files that did not appear in the results
        LinkedList<File> otherResults = new LinkedList<>();
        for (int i = 0; i < stemResults.size(); i++) {
            if (!results.contains(stemResults.get(i)))
                otherResults.add(stemResults.get(i));
        }
        
        if (graphic == 0 || graphic == 2) {
            if (otherResults.isEmpty()) {
                writer.println("Searches for similar words yielded the same results");
            } else {
                writer.println("Search Results for similar words:");
                for (int i = 0; i < otherResults.size(); i++) {
                    writer.println(otherResults.get(i).getName());
                    for (int j = 0; j < stemmedWords.length; j++) {
                        if (!stopList.contains(stemmedWords[i]))
                            writer.println("\t\"..." + snippet(length, stemmedWords[j], otherResults.get(i), true) + "...\"");
                    }
                }
            }
        }
        writer.close();
        
        //Display results to gui
        if (graphic == 1 || graphic == 2) {
            displayWordsStemmed(query, results, otherResults, words, stemmedWords, length);
        }
    }
    
    /**
     * This method will display in a gui the results from searching for one word in the query
     * @param word The word that is being searched for
     * @param visited The list of files that the word appears in with the number of appearances
     * @param length The size of the snippet
     * @throws FileNotFoundException If file is not found
     */
    private void displayWord (String word, Hashtable<File, Integer> visited, int length) throws FileNotFoundException {
        LinkedList<String> out = new LinkedList<>();
        Displayer d = new Displayer(word);
        
        if (visited == null) {
            out.add("No results found");
            d.displayNonStem(out.toArray());
            return;
        }
        
        Set<Entry<File, Integer>> vSet = visited.entrySet();
        for (Entry<File, Integer> e : vSet) {
            String o = "<html>" + e.getKey().getName() + "<br>";
            o += "Number of Appearances " + e.getValue() + "<br>"; 
            o += "First appearance: \"...<xmp>" + snippet(length, word, e.getKey(), false) + "</xmp>...\"</html>";
            out.add(o);
        }
        
        d.displayNonStem(out.toArray());
    }
    
    /**
     * Displays in a gui the results from searching for one word using stemming
     * @param word The word searched for by the user
     * @param stemmedWord The stem of the word after going through Porter's Algorithm
     * @param visited The list of files where the word appears and the number of times it does
     * @param visitedStemmed The list of files where the stemmed word appears and the number of times it does
     * @param length The length of the snippet as set by the user
     * @throws FileNotFoundException If file is not found
     */
    private void displayWordStemmed (String word, String stemmedWord, Hashtable<File, Integer> visited,
            Hashtable<File, Integer> visitedStemmed, int length) throws FileNotFoundException {
        //If the stemmed word was the same, then visitedStemmed would be null and displayWord would be called instead
        if (visitedStemmed == null) {
            displayWord(word, visited, length);
            return;
        }
        
        Displayer d = new Displayer(word);
        LinkedList<String> out = new LinkedList<>();
        LinkedList<String> outS = new LinkedList<>();
        
        if (visited == null) {
            out.add("No results found");
        } else {
            Set<Entry<File, Integer>> vSet = visited.entrySet();
            for (Entry<File, Integer> e : vSet) {
                String o = "<html>" + e.getKey().getName() + "<br>";
                o += "Number of Appearances " + e.getValue() + "<br>"; 
                o += "First appearance: \"...<xmp>" + snippet(length, word, e.getKey(), false) + "</xmp>...\"</html>";
                out.add(o);
            }
        }
        
        Set<Entry<File, Integer>> sSet = visitedStemmed.entrySet();
        for (Entry<File, Integer> e : sSet) {
            String o = "<html>" + e.getKey().getName() + "<br>";
            o += "Number of Appearances " + e.getValue() + "<br>";
            o += "First appearance: \"...<xmp>" + snippet(length, stemmedWord, e.getKey(), true) + "</xmp>...\"</html>";
            outS.add(o);
        }
        
        d.displayStem(stemmedWord, out.toArray(), outS.toArray());
    }
    
    /**
     * Displays multi-word queries to a GUI, without stemming
     * @param query The user inputted query
     * @param results The list of files that have all the non-stopword query terms
     * @param words The list of all words in the query
     * @param length The size of the snippet as indicated by the user
     * @throws FileNotFoundException If the file cannot be found
     */
    private void displayWords(String query, LinkedList<File> results, String[] words, int length) throws FileNotFoundException {
        LinkedList<String> out = new LinkedList<>();
        Displayer d = new Displayer(query);
        
        if (results == null) {
            out.add("No results found");
            d.displayNonStem(out.toArray());
            return;
        }
        
        for(int i = 0; i < results.size(); i++) {
            String o = "<html>" + results.get(i).getName() + "<br>";
            for (int j = 0; j < words.length; j++) {
                if (!stopList.contains(words[j].toLowerCase())) {
                    o += "\"...<xmp>" + snippet(length, words[j], results.get(i), false) + "</xmp>...\"";
                    if (j < words.length-1)
                        o += "<br>";
                }
            } 
            o += "</html>";
            out.add(o);
        }
        
        d.displayNonStem(out.toArray());
    }
    
    /**
     * Displays the results for multi word queries with stemming turned on
     * @param query The query string inputed by the user
     * @param results The list of returned files from the search for the original query
     * @param stemmedResults The list of returned files from the search for the stemmed query
     * @param words The query words separated into an array
     * @param stemmedWords The stemmed query words separated into an array
     * @param length The size of the snippet as defined by the user
     * @throws FileNotFoundException If the file path is invalid
     */
    private void displayWordsStemmed(String query, LinkedList<File> results, LinkedList<File> stemmedResults,
            String[] words, String[] stemmedWords, int length) throws FileNotFoundException {
        LinkedList<String> out = new LinkedList<>();
        LinkedList<String> outS = new LinkedList<>();
        Displayer d = new Displayer(query);
        
        if (results == null) {
            out.add("No results found");
        } else {
            for (int i = 0; i < results.size(); i++) {
                String o = "<html>" + results.get(i).getName() + "<br>";
                for (int j = 0; j < words.length; j++) {
                    if (!stopList.contains(words[j].toLowerCase())) {
                        o += "\"...<xmp>" + snippet(length, words[j], results.get(i), false) + "</xmp>...\"";
                        if (j < words.length-1)
                            o += "<br>";
                    }
                }
                o += "</html>";
                out.add(o);
            }
        }
        
        if (stemmedResults.isEmpty()) {
            outS.add("Results for similar searches are the same");
        } else {
            for (int i = 0; i < stemmedResults.size(); i++) {
                String o = "<html>" + results.get(i).getName() + "<br>";
                for (int j = 0; j < stemmedWords.length; j++) {
                    if (!stopList.contains(stemmedWords[i])) {
                        o += "\"...<xmp>" + snippet(length, stemmedWords[j], stemmedResults.get(i), true) + "</xmp>...\"";
                        if (j < stemmedWords.length-1)
                            o += "<br>";
                    }
                }
                o += "</html>";
                outS.add(o);
            }
        }
        
        d.displayStem("", out.toArray(), outS.toArray());
    }
}
