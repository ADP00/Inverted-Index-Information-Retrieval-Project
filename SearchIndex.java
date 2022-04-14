import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * SearchIndex takes in the command line flags and parses them to see what the 
 * user wants to search and output in the inverted index.
 * @author Alex Perinetti
 *
 */

public class SearchIndex {
    
    /**
     * Main method that parses the command line arguments and checks that they are the proper flags. 
     * After parsing, the inverted index is created and the indicated searches and printing is done.
     * @param args The command line arguments/flags
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        int i = 0;
        int length = 5;
        int graphic = 0;
        String flag;
        String dir = "./Corpus";
        String search = "";
        String output = "./output.txt";
        String print = "";
        String printOutput = "./printOutput.txt";
        boolean help = false;
        boolean error = false;
        boolean isWord = true;
        boolean isWords = false;
        boolean hasSearch = false;
        boolean hasPrint = false;
        boolean printWord = false;
        boolean stemmed = false;
        boolean fileInput = false;
        
        //While loop for parsing the command line
        while (i < args.length && args[i].startsWith("-")) {
            flag = args[i++];
            
            if (flag.equals("-h")) {
                help = true;
            } else if (flag.equals("-s")) {
                stemmed = true;
            } else {
                int equalIndex = flag.indexOf('=');
                if (equalIndex < 0) {
                    System.out.println("Format of input is incorrect");
                    error = true;
                    break;
                }
                
                if (flag.substring(0, equalIndex).equals("-SEARCH")) {
                    hasSearch = true;
                    if (flag.substring(equalIndex+1).equals("WORD")) {
                        isWord = true;
                    } else if (flag.substring(equalIndex+1).equals("DOC")) {
                        isWord = false;
                    } else if (flag.substring(equalIndex+1).equals("WORDS")) {
                        isWord = false;
                        isWords = true;
                    } else if (flag.substring(equalIndex+1).equals("FILE")) {
                        fileInput = true;
                        isWord = false;
                    } else {
                        error = true; 
                        break;
                    }
                    search = args[i++];
                } else if (flag.substring(0, equalIndex).equals("-dir")) {
                    dir = flag.substring(equalIndex+1);
                } else if (flag.substring(0, equalIndex).equals("-output")) {
                    output = flag.substring(equalIndex+1);
                } else if (flag.substring(0, equalIndex).equals("-PRINT_INDEX")) {
                    hasPrint = true;
                    if (flag.substring(equalIndex+1).equals("WORD")) {
                        printWord = true;
                    } else if (flag.substring(equalIndex+1).equals("DOC")) {
                        printWord = false;
                    } else {
                        error = true;
                        break;
                    }
                    print = args[i++];
                } else if (flag.substring(0, equalIndex).equals("-printOutput")) {
                    printOutput = flag.substring(equalIndex+1);
                } else if (flag.substring(0, equalIndex).equals("-len")) {
                    String len = flag.substring(equalIndex+1);
                    try {
                        length = Integer.parseInt(len);
                        if (length < 0) {
                            error = true;
                            break;
                        }
                    } catch (NumberFormatException e) {
                        error = true;
                        break;
                    }
                } else if (flag.substring(0, equalIndex).equals("-graphic")) {
                    String g = flag.substring(equalIndex+1);
                    try {
                        graphic = Integer.parseInt(g);
                        if (graphic > 2 || graphic < 0) {
                            error = true;
                            break;
                        }
                    } catch (NumberFormatException e) {
                        error = true;
                        break;
                    }
                }
                else {
                    error = true;
                    System.out.println("Invalid Command");
                    break;
                }
            }
        }
        
        //If an error occurs or the help flag is indicated, output the format for input to user
        if (error || help || hasSearch && (search.equals("") || search.startsWith("-")) 
                || hasPrint && (print.equals("") || print.startsWith("-"))) {
            System.out.println("Usage: SearchIndex [-SEARCH=(WORD word|DOC \"doc name\"|WORDS \"query string\""
                    + "|FILE \"input file name containing multiple queries\")]"
                    + " [-dir=CorpusDirectory] [-output=OutputFile] [-h] [-len=(Number)] "
                    + "[-PRINT_INDEX=(WORD word|DOC \"doc name\")] [-s] [-printOutput=PrintOutputFile] "
                    + "[-graphic=(0|1|2) /*0=file output, 1=graphical output, 2=both outputs*/]");
            if (error || hasSearch && (search.equals("") || search.startsWith("-")) 
                    || hasPrint && (print.equals("") || print.startsWith("-"))) return;
        }
        
        InvertedIndex index = new InvertedIndex(dir);
        
        //Search for word or doc in the Inverted Index
        if(hasSearch) {
            //Do normal search of stemmed flag is not triggered
            if (!stemmed) {
                if (fileInput) { //Reads the queries from an input file provided by the user
                    FileInputStream fis;
                    try {
                        fis = new FileInputStream(search);
                        BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                        String line;
                        int outputNum = 1;
                        String fileName = extractFileName(output);
                        while ((line = br.readLine()) != null) {
                            String[] words = line.split(" ");
                            //Uses the inputed output name for each output file and numbers them
                            if (words.length > 1) {
                                index.searchWords(line, fileName + "(" + outputNum + ").txt", length, graphic);
                            } else if (words.length == 1) {
                                index.searchWord(line, fileName + "(" + outputNum + ").txt", length, graphic);
                            }
                            outputNum ++;
                        }
                        br.close();
                        fis.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        System.out.println("Input file cannot be read");
                        return;
                    }
                } else if (isWord) {
                    try {
                        index.searchWord(search, output, length, graphic);
                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                } else if (!isWord && !isWords){
                    try {
                        index.searchDoc(search, output);
                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else if (isWords) { //If WORDS is selected, search for multiple words in the corpus
                    try {
                        index.searchWords(search, output, length, graphic);
                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } 
            } else { //Do stemmed search for a word if stemmed flag is enabled, doc stays the same
                index.makeStemmedIndex();
                if (fileInput) { //Reads the queries from an input file provided by the user, with stem
                    FileInputStream fis;
                    try {
                        fis = new FileInputStream(search);
                        BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                        String line;
                        int outputNum = 1;
                        String fileName = extractFileName(output);
                        while ((line = br.readLine()) != null) {
                            String[] words = line.split(" ");
                            //Uses the inputed output name for each output file and numbers them
                            if (words.length > 1) {
                                index.searchWordsStemmed(line, fileName + "(" + outputNum + ").txt", length, graphic);
                            } else if (words.length == 1) {
                                index.searchWordStemmed(line, fileName + "(" + outputNum + ").txt", length, graphic);
                            }
                            outputNum ++;
                        }
                        br.close();
                        fis.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        System.out.println("Input file cannot be read");
                        return;
                    }
                } else if (isWord) {
                    try {
                        index.searchWordStemmed(search, output, length, graphic);
                    } catch(FileNotFoundException e) {
                        e.printStackTrace();
                    }
                } else if(!isWord && !isWords) {
                    try {
                        index.searchDoc(search, output);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                } else if (isWords) {
                    try {
                        index.searchWordsStemmed(search, output, length, graphic);
                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }  
        }
        
        //Print word or doc from the Inverted Index
        if(hasPrint) {
            if (printWord) {
                try {
                    index.printWord(print, printOutput);
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {
                try {
                    index.printDoc(print, printOutput);
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    private static String extractFileName(String name) {
        int index = name.lastIndexOf('.');
        return name.substring(0, index);
    }
}
