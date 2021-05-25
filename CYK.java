import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

public class CYK {

  private Map<Character, ArrayList<String>> gramaticas;
  private String buildChain;

  public CYK(String[] input, String buildChain) {
    this.gramaticas = developProductions(input);
    this.buildChain = buildChain;



    //Print all the info about the string
    if (chomskyVerification(gramaticas)) {
      System.out.println("Grammar is in Chomsky Normal Form");
      if (CYK_MAP(gramaticas, buildChain)) {
        System.out.println("The string belongs to the language of grammar");
      } else {
        System.out.println(
          "The string does not belong to the language of grammar"
        );
      }
    } else {
      chomskySteps(gramaticas);
      System.out.println("Grammar is now in Chomsky Normal Form");
      if (CYK_MAP(gramaticas, buildChain)) {
        System.out.println("\n\nThe string belongs to the language of grammar");
      } else {
        System.out.print(
          "The string does not belong to the language of grammar"
        );
      }
    }
  }

  private Map<Character, ArrayList<String>> developProductions(
    String[] userStr
  ) {
    if (userStr == null) {
      return null;
    }

    Map<Character, ArrayList<String>> grammarsDic = new Hashtable<>();

    for (String str : userStr) {
      if (!grammarsDic.containsKey(str.charAt(0))) {
        String productions = str.substring(4);

        grammarsDic.put(
          str.charAt(0),
          new ArrayList<>(Arrays.asList(productions.split("\\|")))
        );
      }
    }

    return grammarsDic;
  }

  //Start by doing the chomsky steps and valuidate each step
  public Map<Character, ArrayList<String>> chomskySteps(
    Map<Character, ArrayList<String>> grammars
  ) {
    //Remove epsilon characters
    handleEpsilon(grammars);
    //Remove the unitary productions
    replaceUnitaryProduction(grammars);
    //Remove the waste simbols
    removeNonUsefulProductions(grammars);
    //Replace the terminal characters/states/simbols
    replaceTerminalCharacters(grammars);
    //Replace the non terminal characters/states/simbols
    replaceNonTerminals(grammars); 
    return grammars;
  }

  //Start by doing chomsky verification
  private boolean chomskyVerification(
    Map<Character, ArrayList<String>> grammarsDic
  ) {
    //Normal verification
    for (Map.Entry<Character, ArrayList<String>> entry : grammarsDic.entrySet()) {
      ArrayList<String> prod = entry.getValue();

      for (String str : prod) {
        if (str.length() >= 3) {
          return false;
        }

        int nonTerminalStates = 0;

        for (int i = 0; i < str.length(); i++) {
          //If epsilon
          if (str.charAt(i) == 'e' && str.length() == 1) {
            return false;
          }

          // If terminal
          if (str.charAt(i) > 97 && str.length() > 1) {
            return false;
          }

          //Productions
          if (str.charAt(i) > 64 && str.charAt(i) < 95) {
            nonTerminalStates++;
          }

          //Unitary production
          if (
            nonTerminalStates > 2 ||
            (nonTerminalStates == 1 && str.length() == 1)
          ) {
            return false;
          }

          //Production non useful
          if (
            (
              str.charAt(0) == entry.getKey().charValue() && str.length() == 1
            ) ||
            (
              str.charAt(0) == entry.getKey().charValue() &&
              str.charAt(1) == entry.getKey().charValue()
            )
          ) {
            return false;
          }
        }
      }
    }
    return true;
  }

  //Remove the epsilon
  private void handleEpsilon(Map<Character, ArrayList<String>> grammars) {
    ArrayList<Character> removable = new ArrayList<>();
    for (Map.Entry<Character, ArrayList<String>> entry : grammars.entrySet()) {
      ArrayList<String> productions = entry.getValue();
      boolean isEmpty = false;
      for (String production : productions) {
        //Handle epsilon found
        if (production.charAt(0) == 'e' && production.length() == 1) {
          removable.add(entry.getKey());
          productions.remove(production);
          isEmpty = !isEmpty;
        }
        if (isEmpty) {
          break;
        }
      }
    }

    //Replace the non terminal states
    for (Map.Entry<Character, ArrayList<String>> entry : gramaticas.entrySet()) {
      ArrayList<String> productions = entry.getValue();
      ArrayList<String> tmpList = new ArrayList<>();
      boolean isProduction = false;

      for (String production : productions) {
        StringBuilder tmpProduction = new StringBuilder();
        for (int i = 0; i < production.length(); i++) {
          if (!removable.contains(production.charAt(i))) {
            tmpProduction.append(production.charAt(i));
          } else {
            isProduction = true;
          }
        }
        if (isProduction) {
          tmpList.add(tmpProduction.toString());
          isProduction = false;
        }
      }
      for (String str : tmpList) {
        productions.add(str);
      }
    }
  }

  //Add productions of unitary production
  private void replaceUnitaryProduction(
    Map<Character, ArrayList<String>> grammars
  ) {
    for (Map.Entry<Character, ArrayList<String>> entry : grammars.entrySet()) {
      ArrayList<String> productions = entry.getValue();
      ArrayList<String> tmpList = new ArrayList<>();
      for (String production : productions) {
        if (
          production.charAt(0) > 64 &&
          production.charAt(0) < 91 &&
          production.length() == 1
        ) {
          ArrayList<String> reemplazador = gramaticas.get(production.charAt(0));
          productions.remove(production);
          tmpList.addAll(reemplazador);
        }
      }
      tmpList.addAll(productions);
      gramaticas.put(entry.getKey(), tmpList);
    }
  }

  private void removeNonUsefulProductions(
    Map<Character, ArrayList<String>> grammars
  ) {
    ArrayList<Character> nonUsefulProductions = new ArrayList<>();
    for (Map.Entry<Character, ArrayList<String>> entry : grammars.entrySet()) {
      ArrayList<String> productions = entry.getValue();
      for (String production : productions) {
        if (
          (
            production.charAt(0) == entry.getKey().charValue() &&
            production.length() == 1
          ) ||
          (
            production.charAt(0) == entry.getKey().charValue() &&
            production.charAt(1) == entry.getKey().charValue()
          )
        ) {
          nonUsefulProductions.add(entry.getKey());
        }
      }
    }

    //Remove the non useful production
    for (Character c : nonUsefulProductions) {
      grammars.remove(c);
    }

    //Handling the remove
    for (Map.Entry<Character, ArrayList<String>> entry : grammars.entrySet()) {
      ArrayList<String> productions = entry.getValue();
      ArrayList<String> usefulProductions = new ArrayList<>();
      boolean remove = false;
      if (nonUsefulProductions.contains(entry.getKey())) {
        remove = true;
      } else {
        for (String production : productions) {
          boolean util = true;
          for (int i = 0; i < production.length(); i++) {
            if (nonUsefulProductions.contains(production.charAt(i))) {
              util = false;
            }
          }

          if (util) {
            usefulProductions.add(production);
          }
        }
      }
      grammars.put(entry.getKey(), usefulProductions);
    }

    //Por si alguna gramatica ya no se manda a llamar porque estaba concatenada con una inutil
    Set<Character> simbolArry = new HashSet<>(gramaticas.keySet());
    for (Map.Entry<Character, ArrayList<String>> entry : gramaticas.entrySet()) {
      ArrayList<String> productions = entry.getValue();
      for (String production : productions) {
        for (int i = 0; i < production.length(); i++) {
          if (simbolArry.contains(production.charAt(i))) {
            simbolArry.remove(production.charAt(i));
          }
        }
      }
    }

    for (Character simbol : simbolArry) {
      gramaticas.remove(simbol);
    }
  }

  private void replaceTerminalCharacters(
    Map<Character, ArrayList<String>> grammars
  ) {
    Set<Character> pastProductionsSet = new HashSet<>();

    int asciiStart = 65;

    for (Map.Entry<Character, ArrayList<String>> entry : grammars.entrySet()) {
      if (!pastProductionsSet.contains(entry.getKey())) {
        pastProductionsSet.add(entry.getKey());
      }
    }

    //Add the new productions
    Map<Character, Character> terminalProductionMap = new Hashtable<>();
    Map<Character, ArrayList<String>> newProductionsMap = new Hashtable<>();

    //Replace the new productions
    for (Map.Entry<Character, ArrayList<String>> entry : grammars.entrySet()) {
      ArrayList<String> productions = entry.getValue();
      ArrayList<String> terminalReplacement = new ArrayList<>();
      for (String production : productions) {
        for (int i = 0; i < production.length(); i++) {
          if (production.charAt(i) >= 97) { //terminal
            char c = (char) asciiStart;
            if (!terminalProductionMap.containsKey(production.charAt(i))) {
              while (
                newProductionsMap.containsKey(c) ||
                pastProductionsSet.contains(c)
              ) { //Encontrar letra no terminal
                asciiStart++;
                c = (char) asciiStart;
              }
              terminalProductionMap.put(production.charAt(i), c);
              ArrayList<String> terminal = new ArrayList<>();
              terminal.add(production.charAt(i) + "");
              newProductionsMap.put(c, terminal);
              production = production.replace(production.charAt(i), c);
            } else {
              production =
                production.replace(
                  production.charAt(i),
                  terminalProductionMap.get(production.charAt(i))
                );
            }
          }
        }
        terminalReplacement.add(production);
      }
      grammars.put(entry.getKey(), terminalReplacement);
    }
    grammars.putAll(newProductionsMap);
  }

  private void replaceNonTerminals(Map<Character, ArrayList<String>> grammars) {
    Map<Character, ArrayList<String>> nonTerminalProductionsMap = new Hashtable<>();

    int simbols = 65; //Código Ascii

    for (Map.Entry<Character, ArrayList<String>> entry : grammars.entrySet()) {
      ArrayList<String> productions = entry.getValue();
      ArrayList<String> newProductions = new ArrayList<String>();
      for (String production : productions) {
        int nonTerminalCount = 0;
        boolean flag = false;
        for (int i = production.length() - 1; i >= 0; i--) {
          if (flag) {
            i = production.length() - 1;
            flag = false;
          }
          if (production.charAt(i) > 64 && production.charAt(i) < 91) {
            nonTerminalCount++;
          }

          if (nonTerminalCount == 2 && (i - 1) >= 0) { //Nueva production o Cambiar de 2 noTerminales a 1
            char c = (char) simbols;
            if (
              nonTerminalProductionsMap.containsValue(
                production.substring(i, i + 2)
              )
            ) {
              boolean encontrado = false;
              for (Map.Entry<Character, ArrayList<String>> iterator : nonTerminalProductionsMap.entrySet()) {
                if (encontrado) break;
                ArrayList<String> tmpList = entry.getValue();
                for (String str : tmpList) {
                  if (str.equals(production.substring(i, i + 2)));
                  encontrado = true;
                }
              }
            } else {
              c = (char) simbols;
              while (
                grammars.containsKey(c) ||
                nonTerminalProductionsMap.containsKey(c)
              ) {
                simbols++;
                c = (char) simbols;
              }
            }
            flag = true;
            ArrayList<String> str = new ArrayList<>(); //production de dos terminales
            str.add(production.substring(i, i + 2));
            nonTerminalProductionsMap.put(c, str);
            nonTerminalCount = 0;
            production = production.substring(0, i) + c;
          }
        }
        newProductions.add(production);
      }
      grammars.put(entry.getKey(), newProductions);
    }
    grammars.putAll(nonTerminalProductionsMap);
  }

  private boolean CYK_MAP(
    Map<Character, ArrayList<String>> grammars,
    String buildChain
  ) {
    ArrayList<ArrayList<ArrayList<Character>>> table = new ArrayList<ArrayList<ArrayList<Character>>>();
    int size = buildChain.length();
    for (int i = 0; i < buildChain.length() + 1; i++) { //row
      ArrayList<ArrayList<Character>> column = new ArrayList<>();
      for (int j = 0; j < size; j++) {
        if (i == 0) {
          ArrayList<Character> simbolArry = new ArrayList<>();
          simbolArry.add(buildChain.charAt(j));
          column.add(simbolArry);
        } else if (i == 1) {
          ArrayList<Character> newSimbols = new ArrayList<>();
          for (Map.Entry<Character, ArrayList<String>> entry : grammars.entrySet()) {
            ArrayList<String> productions = entry.getValue();

            String terminalProduction = productions.get(0);
            Character lastCharacter = table.get(i - 1).get(j).get(0);
            if (terminalProduction.charAt(0) == lastCharacter) {
              newSimbols.add(entry.getKey());
              column.add(newSimbols);
              break;
            }
          }
          if (newSimbols.isEmpty()) {
            return false;
          }
        } else {
          int m = 1;
          int n = i - 1;
          int x = j + 1;
          ArrayList<Character> newSimbols = new ArrayList<>();
          boolean foundProduction = false;

          while (m < i) {
            ArrayList<Character> column1 = table.get(m).get(j);
            ArrayList<Character> column2 = table.get(n).get(x);

            for (Character c1 : column1) {
              for (Character c2 : column2) {
                if (c1 != null && c2 != null) {
                  String merge = "" + c1 + c2;

                  for (Map.Entry<Character, ArrayList<String>> entry : gramaticas.entrySet()) {
                    ArrayList<String> nonTerminal = entry.getValue();
                    for (String production : nonTerminal) {
                      if (production.equals(merge)) {
                        newSimbols.add(entry.getKey());
                        foundProduction = true;
                      }
                    }
                  }
                }
              }
            }
            m++;
            n--;
            x++;
          }
          if (!foundProduction) {
            newSimbols.add(null);
          }
          column.add(newSimbols);
        }
      }
      table.add(column);
      if (i > 0) size--;
    }

    ArrayList<Character> checkLastSimbol = table
      .get(buildChain.length())
      .get(0);

    for (Character c : checkLastSimbol) {
      if (c == null) {
        return false;
      }

      if (c == 'S') {
        Node<Character> node = new Node<Character>('S');
        DerivationTree<Node<Character>> tree = new DerivationTree(node);
        derivationTree(tree.root, table, table.size() - 1, 0);
        System.out.println("Level 0: " + tree.root.getInfo());
        printByLevel(tree.root.left, tree.root.right, 1);
        return true;
      }
    }

    return false;
  }

  //Methods for tree print and build
  public void derivationTree(
    Node root,
    ArrayList<ArrayList<ArrayList<Character>>> table,
    int row,
    int column
  ) {
    Node<Character> tmp = root;
    Node<Character> tmp2 = root;
    root.setInfo(table.get(row).get(column).get(0));
    int terminales = 0;
    int indexR = row;
    int indexC = column;

    while (terminales < buildChain.length()) {
      while (indexR > 0) {
        int indexRtmp = indexR - 1;
        int indexCtmp = indexC + 1;
        if (tmp != null) {
          tmp2 = tmp;
          while (indexRtmp > 0) {
            if (table.get(indexRtmp).get(indexCtmp).get(0) != null) {
              tmp.right =
                new Node<Character>(table.get(indexRtmp).get(indexCtmp).get(0));
              tmp = tmp.getRight();
            }
            indexRtmp--;
            indexCtmp++;
          }
          indexRtmp = indexR - 1;
          indexCtmp = indexC;
          while (indexRtmp > 0) {
            if (table.get(indexRtmp).get(indexCtmp).get(0) != null) {
              tmp = tmp2;
              tmp.left =
                new Node<Character>(table.get(indexRtmp).get(indexCtmp).get(0));
              tmp = tmp.left;
              int fila = indexRtmp - 1;
              int columna = indexCtmp + 1;
              Node tmpRight = tmp;
              while (fila > 0) {
                tmp2 = tmp;
                if (table.get(fila).get(columna).get(0) != null) {
                  tmpRight.right =
                    new Node<Character>(table.get(fila).get(columna).get(0));

                  tmpRight = tmpRight.getRight();
                }
                fila--;
                columna++;
              }
            }
            indexRtmp--;
          }
        }
        indexR--;
        indexC++;
        tmp = tmp2.right;
        terminales++;
      }
    }
  }

  public void printByLevel(Node left, Node right, int level) {
    //Impresion por nivel separados por coma
    if (left != null) {
      System.out.print("Level " + level + ": " + left.getInfo() + ", ");
      System.out.println();
      printByLevel(left.left, left.right, level + 1);
    }

    if (right != null) {
      System.out.print(right.getInfo());
      System.out.println();
      printByLevel(right.left, right.right, level + 1);
    }
  }

  public static void main(String[] args) {
    String[] testInput = { "S-> aSb|e" };

    String buildChain = "aaabbb";

    CYK algorithm = new CYK(testInput, buildChain);
  }

  //Auxiliar class to print the derivation tree

  private class DerivationTree<E> {

    private Node<E> root;

    public DerivationTree(Node<E> node) {
      this.root = node;
    }
  }

  private class Node<E> {

    private E info;
    private Node<E> left;
    private Node<E> right;

    public Node(E info) {
      this.info = info;
      this.left = null;
      this.right = null;
    }

    public E getInfo() {
      return info;
    }

    public void setInfo(E info) {
      this.info = info;
    }

    public Node<E> getRight() {
      return right;
    }
  }
}
