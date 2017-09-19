package bit.minisys.minicc.scanner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Stack;
//for xml output
import org.jdom.Document;  
import org.jdom.Element;  
import org.jdom.JDOMException;  
import org.jdom.output.Format;  
import org.jdom.output.XMLOutputter; 
/***
 * version 1.0
 * use double buffer
 * @author fanyijie
 *
 */
public class MiniCCScanner implements IMiniCCScanner {

	//cur is for tracking the position in code like a cursor
	private static int cur;
	//fence is the start index of buffer
	private static int fence;
	//endcur is the end index of buffer, cur should not bigger or equal it
	private static int endcur;
	//the position in the whole file
	private static int pos;
	//number is just a ascend index mark
	private static char[] buf;
	//8000 should be better
	private static final int BUF_SIZE = 8000; 
	//number means the Nth word
	private int number;
	//line means the line number in the original file
	private int line;
	private static String FILE_NAME;
	private static String OFILE_NAME;
	private static boolean endFlag = false;
	
	//current char
	private static char c;
	/***
	 * @param iFile
	 * the name of input file
	 * @param ofile
	 * the name of the output file
	 * 
	 */
	@Override
	public void run(String iFile, String oFile) throws IOException {
		System.out.println(iFile);
		FILE_NAME = iFile;
		OFILE_NAME = oFile;
		init();
		tokenizer();
		System.out.println("lex finish");
	}
	/***
	 * 
	 * @param input
	 * @return output
	 */
	public void tokenizer() {
		cur = 0;
		number = 0;
		ArrayList<Token> tokens = new ArrayList<Token>();
		//for peek temp use
		char nc;
		//cur can be edit in the lex_ function, so maybe next char is not one by one
		//notice: when use nextChar, the cursor has been update to next
		next();
		while(!endFlag) {
			KLog.d("new switc turn, char is " + c + " value is " + Integer.valueOf(c));
			switch (c) {

			//white space
			//here should have \v
			case ' ': case '\t': case '\f': case '\0': case '\r':
				next();
				break;
				//identifier
			 	case '_':
			    case 'a': case 'b': case 'c': case 'd': case 'e': case 'f':
			    case 'g': case 'h': case 'i': case 'j': case 'k': case 'l':
			    case 'm': case 'n': case 'o': case 'p': case 'q': case 'r':
			    case 's': case 't':           case 'v': case 'w': case 'x':
			    case 'y': case 'z':
			    case 'A': case 'B': case 'C': case 'D': case 'E': case 'F':
			    case 'G': case 'H': case 'I': case 'J': case 'K':
			    case 'M': case 'N': case 'O': case 'P': case 'Q':
			    case 'S': case 'T':           case 'V': case 'W': case 'X':
			    case 'Y': case 'Z':
			    	tokens.add(lexIdentifier());
			    	break;
			    	
			    	
			    //number	
			 case '0': case '1': case '2': case '3': case '4':
			 case '5': case '6': case '7': case '8': case '9':
			    tokens.add(lexNumber());
			    break;
			    //separator
			 case '[': case ']': case'(': case ')': case '{' :case '}': case ';':
				 tokens.add(new Token(++number, String.valueOf(c), Type.SEPARATOR, line, true));
				 next();
				 break;
				 
				 
			 //operator
			 case '<' :
				 nc = peek();
				 if (nc == '=') {
					 tokens.add(new Token(++number, "<=", Type.OPERATOR, line, true));
					 next2();
				 } else if(nc == '<') {
					 if(peek2() == '=') {
						 tokens.add(new Token(++number, "<<=", Type.OPERATOR, line, true));
						 next3();
					 } else {
						 tokens.add(new Token(++number, "<<", Type.OPERATOR, line, true));
						 next2();
					 }
				 } //should only when digraph turn on the option below
				 else if (nc == ':') {
					 tokens.add(new Token(++number, "<:", Type.OPERATOR, line, true));
					 next2();
				 } else if (nc == '%') {
					 tokens.add(new Token(++number, "<%", Type.OPERATOR, line, true));
					 next2();
				 } else {
					 tokens.add(new Token(++number, "<", Type.OPERATOR,Mark.LOWER, line, true));
					 next();
				 }
				 break;
			 case '>' :
				 nc = peek();
				 if (nc == '=') {
					 tokens.add(new Token(++number, ">=", Type.OPERATOR, line, true));
					 next2();
				 } else if (nc == '>') {
					 if (peek2() == '=') {
						 tokens.add(new Token(++number, ">>=", Type.OPERATOR, line, true));
						 next3();
					 } else {
						 tokens.add(new Token(++number, ">>", Type.OPERATOR, line, true));
						 next2();
					 }
				 } else {
					 tokens.add(new Token(++number, ">", Type.OPERATOR,Mark.GREATER, line, true));
					 next();
				 } 
				 break;
			 case '%':
				 nc = peek();
				 if(nc == '=') {
					tokens.add(new Token(++number, "%=", Type.OPERATOR, line, true));
					 next2();
				 } //TODO: here should only work when cpp option are digraph
				 else if(peek2() == ':') {
					 if(peek3() == '%' && peek4() == ':') {
						 tokens.add(new Token(++number, "%:%:", Type.OPERATOR, line, true));
						 next2();
						 next2();
					 } else {
						 tokens.add(new Token(++number, "%:", Type.OPERATOR, line, true));
						 next2();
					 }
				} else if (peek2() == '>') {
					tokens.add(new Token(++number, "%>:", Type.OPERATOR, line, true));	
					next();
				 } else {
					 tokens.add(new Token(++number, "%", Type.OPERATOR, line, true));
					 next();
				 }
			 case '.':
				 nc = peek();
				 if(isDigit(nc)) {
					 tokens.add(lexNumber());
				 } else if(nc == '.' && peek2() == '.') {
					 tokens.add(new Token(++number, "...", Type.OPERATOR, line, true));
					 next2();
					 next();
				 } //if is c++, .* is also should be a mark
				 else {
					 tokens.add(new Token(++number, ".", Type.OPERATOR, line, true));
					 next();
				 }
				 
			 case '+': 
				 nc = peek();
				 if (nc == '+') {
					 tokens.add(new Token(++number, "++", Type.OPERATOR, line, true));
					 next2();
				 } else if(nc == '=') {
					 tokens.add(new Token(++number, "+=", Type.OPERATOR, line, true));
					 next2();
				 } else {
					 tokens.add(new Token(++number, "+", Type.OPERATOR, Mark.PLUS, line, true));
					 next();
				 }
				 break;
			 case '-': 
				 nc = peek();
				 if (nc == '+') {
					 tokens.add(new Token(++number, "--", Type.OPERATOR, line, true));
					 next2();
				 } else if(nc == '=') {
					 tokens.add(new Token(++number, "-=", Type.OPERATOR, line, true));
					 next2();
				 } else if(nc == '>') {
					 if (peek2() == '*') {
						 tokens.add(new Token(++number, "->*", Type.OPERATOR, line, true));
						 next3();
					 } else {
						 //TODO: turn &gt; to >
						 tokens.add(new Token(++number, "->", Type.OPERATOR, line, true));
						 next2();
					 }
				 }
				 else {
					 tokens.add(new Token(++number, "-", Type.OPERATOR,Mark.MINUS, line, true));
					 next();
				 }
				 break;
			 case '*': case '/': case '=' : case '!': case '^':
				 if (peek() == '=') {
					 tokens.add(new Token(++number, c + "=", Type.OPERATOR, line, true));
					 next2();
				 } else {
					 Mark m = null;
					 if (c == '*') {
						 m = Mark.MUL;
					 } else if (c== '/') {
						 m = Mark.DIV;
					 }
					 tokens.add(new Token(++number, String.valueOf(c), Type.OPERATOR, m, line, true));
					 next();
				 }
				 break;
				 
			 case '#' :
				 if (peek() == '#') {
					 tokens.add(new Token(++number, "##", Type.OPERATOR, line, true));
					 next2();
				 } else {
					 tokens.add(new Token(++number, "#", Type.OPERATOR, line, true));
					 next();
				 }
				 break;
			 case '?': case '~' : case ',': case '@':
				 tokens.add(new Token(++number, String.valueOf(c), Type.OPERATOR, line, true));
				 next();
				 break;
				 
				 
				 
			//byte operator
			 case '&':
				 nc = peek();
				 if (nc == '&') {
					 tokens.add(new Token(++number, "&&", Type.OPERATOR, line, true));
					 next2();
				 } else if(nc == '=') {
					 tokens.add(new Token(++number, "&=", Type.OPERATOR, line, true));
					 next2();
				 } else {
					 tokens.add(new Token(++number, "&", Type.OPERATOR, line, true));
					 next();
				 }
				 break;
			 case '|' :
				 nc = peek();
				 if (nc == '|') {
					 tokens.add(new Token(++number, "||", Type.OPERATOR, line, true));
					 next2();
				 } else if(nc == '=') {
					 tokens.add(new Token(++number, "|=", Type.OPERATOR, line, true));
					 next2();
				 } else {
					 tokens.add(new Token(++number, "|", Type.OPERATOR, line, true));
					 next();
				 }
				 break;
			//colon
			 case ':' :
				 nc = peek();
				 if (nc == ':') {
					 tokens.add(new Token(++number, "::", Type.OPERATOR, line, true));
					 next2();
				 } else if(nc == '>') {
					 tokens.add(new Token(++number, ":>", Type.OPERATOR, line, true));
					 next2();
				 } else {
					 tokens.add(new Token(++number, ":", Type.OPERATOR, line, true));
					 next();
				 }
				 break; 
			 
			 
			 
			 //new line
			 case '\n': 
			    line ++;
			    next();
			   	break;
			 
			 case 4 :
				 endFlag = true;
				 tokens.add(new Token(++number, "#", Type.END, line, true));
				 break;
			 default:
				System.err.println("no match word");
				endFlag = true;
				break;
			  
			}
			
		}
		//output tokens to xml file
		output(tokens);
	}
	private static boolean isDigit() {
		if(c >= 48 && c <= 57){  
            return true;  
        }  
        return false;  
	}
	private static boolean isDigit(char temp) {
		if(temp >= 48 && temp <= 57){  
            return true;  
        }  
        return false;  
	}

	private static boolean isNonDigit(){  
		// [_ | a-z]
        if((c >= 65 && c <= 90) || (c >= 97 && c <= 122) || c == '_'){  
            return true;  
        }  
        return false;  
    }  
	
	//lexical analyze a number which start at cur - 1
	private Token lexNumber() {
		KLog.d("into lex number");
		String value  = "";
		do{
			value += c;
			KLog.d("value: " + value);
			next();
		} while(isDigit() || c == '.');
		return new Token(++number, value, Type.CONSTANT, line, true);
	}
	private Token lexIdentifier() {
		KLog.d("into indentifier number");
		String value  = "";
		do{
			value += c;
			KLog.d("value: " + value);
			next();
		} while(isDigit() || isNonDigit());
		//judge if indentifier should be keyword
		int hash = value.hashCode();
		boolean isKeyword = false;
		for(int i = 0; i < Constants.hashKeyword.length; i++) {
			if (hash == Constants.hashKeyword[i]) {
				isKeyword = true;
			}
		}
		Token token = new Token();
		token.setNumber(++number);
		token.setLine(line);
		token.setValue(value);
		if(isKeyword) {
			if (value.equals("if")) {
				token.setMark(Mark.IF);
			} else if (value.equals("else")) {
				token.setMark(Mark.ELSE);
			} else if (value.equals("while")) {
				token.setMark(Mark.WHILE);
			}
			token.setType(Type.KEYWORD);
		} else {
			token.setType(Type.IDENTIFIER);
		}
		token.setValid(true);
		return token;
	}
	private void init() {
		cur = 0;
		fence = 0;
		pos = 0;
		line = 1;
		buf = new char[2*BUF_SIZE];
		fillBuffer();
		KLog.d("init finish");
		
	}
	//go next
	private static void next() {
		c = buf[cur];
		cur = (cur + 1) % (2*BUF_SIZE);
		if (cur%BUF_SIZE == 0) {
			fillBuffer();
			fence = (cur + BUF_SIZE) % (2*BUF_SIZE);
		}
		KLog.d("get next char: " + c + " value is " + Integer.valueOf(c) );
	 }
	private static void next2() {
		next();
		next();
	}
	private static void next3() {
		next();
		next();
		next();
	}
	//just look next char but not go to next
	private static char peek() {
		char temp = buf[cur];
		KLog.d("char peek is " + temp);
		return temp;
	}
	//just look next next char but not go to next
	private static char peek2() {
		int tempCur = cur;
		cur = (cur + 1) % (2*BUF_SIZE);
		if (cur%BUF_SIZE == 0) {
			fillBuffer();
			fence = (cur + BUF_SIZE) % (2*BUF_SIZE);
		}
		char temp = buf[cur];
		cur = tempCur;
		KLog.d("char peek2 is " + temp);
		return temp;
	}
	private static char peek3() {
		int tempCur = cur;
		cur = (cur + 2) % (2*BUF_SIZE);
		if (cur%BUF_SIZE == 0) {
			fillBuffer();
			fence = (cur + BUF_SIZE) % (2*BUF_SIZE);
		}
		char temp = buf[cur];
		cur = tempCur;
		KLog.d("char peek2 is " + temp);
		return temp;
	}
	private static char peek4() {
		int tempCur = cur;
		cur = (cur + 3) % (2*BUF_SIZE);
		if (cur%BUF_SIZE == 0) {
			fillBuffer();
			fence = (cur + BUF_SIZE) % (2*BUF_SIZE);
		}
		char temp = buf[cur];
		cur = tempCur;
		KLog.d("char peek2 is " + temp);
		return temp;
	}
	//back to one char before
	private static void rollBack() {
		if (cur == fence) {
			//you can not access the char before more than 2*BuffSize
			System.err.println("out of fence bounds!");
			return;
		}
		cur = (cur-1) % (BUF_SIZE * 2);
		KLog.d("rollback finish and cur is " + cur);
	}
	//get prechar without rollback
	private static char preChar() {
		int tempCur = cur;
		if (cur == fence) {
			//you can not access the char before more than 2*BuffSize
			System.err.println("out of fence bounds!");
		}
		cur = (cur-1) % (BUF_SIZE * 2);
		char tempC = buf[cur];
		cur = tempCur;
		KLog.d("pre char finish");
		return tempC;
	}
	//roll back and get char
	private static char backChar() {
		if (cur == fence) {
			//you can not access the char before more than 2*BuffSize
			System.err.println("out of fence bounds!");
		}
		cur = (cur-1) % (BUF_SIZE * 2);
		char tempC = buf[cur];
		KLog.d("back char finish");
		return tempC;
	}
	//fill BUF_SIZE len buffer
	private static void fillBuffer() {	
		 FileInputStream fs = null;
		 BufferedReader reader = null;
		 char[] temp = new char[BUF_SIZE]; 
		 try {
			 fs = new FileInputStream(FILE_NAME);
			 reader = new BufferedReader(new InputStreamReader(fs));
			 int bufSize = BUF_SIZE;
			 int len = -1;
			 KLog.d("position is " + pos);
			 reader.skip(pos);
			 if ((len = reader.read(temp, 0, BUF_SIZE))!= -1)
			 {	
				 KLog.d("fill in buffer");
			     KLog.d(temp);
			     if(len < BUF_SIZE) {
			    	 bufSize = temp.length;
			    	 temp[len] = 4; 
			     }
			     pos = pos + bufSize;
			     System.arraycopy(temp, 0, buf, cur, bufSize);
			     //if endFlag = false, endcur won't be used
			     
			     KLog.d(endcur);
			 } else {
				 endFlag = true;
			 }
		 } catch (IOException e) {
	            e.printStackTrace();
	        } finally {
	            if (reader != null) {
	                try {
	                    reader.close();
	                } catch (IOException e1) {
	                }
	            }
	        } 
	 }
	private void output(ArrayList<Token> tokens) {
		KLog.d("in output");
		Element root = new Element("project").setAttribute("name", OFILE_NAME);
		Document doc = new Document(root);
		Element subroot = new Element("tokens");
		root.addContent(subroot);
		for (Token t : tokens) {
			Element element = new Element("token");
			element.addContent(new Element("number").setText(String.valueOf(t.getNumber())));
			element.addContent(new Element("value").setText(t.getValue()));
			
			String type = String.valueOf(t.getType()).toLowerCase();
			String mark = String.valueOf(t.getMark()).toLowerCase();
			if (type.equals("end")) {
				type = "#";
			}
 			element.addContent(new Element("type").setText(type));
 			element.addContent(new Element("mark").setText(mark));
			element.addContent(new Element("line").setText(String.valueOf(t.getLine())));
			element.addContent(new Element("valid").setText(String.valueOf(t.isValid())));
			subroot.addContent(element);
		}
		 Format format = Format.getPrettyFormat();  
	     XMLOutputter XMLOut = new XMLOutputter(format);  
	     try {
	    	 //here should be OFILE NAME cat to name.l
	    	 XMLOut.output(doc, new FileOutputStream(OFILE_NAME));  
	     } catch (IOException e){
	    	 e.printStackTrace();
	     }
	}
	public static void main(String args[]) {
//		for(int i = 0; i < Constants.keyword.length; i++) {
//			System.out.printf("%d,", Constants.keyword[i].hashCode());
//		}
		
		MiniCCScanner scanner = new MiniCCScanner();
		try {
			scanner.run("test.pp.c", "test.token.xml") ;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
