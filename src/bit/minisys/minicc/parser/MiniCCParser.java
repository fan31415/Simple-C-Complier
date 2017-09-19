package bit.minisys.minicc.parser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import bit.minisys.minicc.parser.Grammar;
import bit.minisys.minicc.scanner.KLog;
import bit.minisys.minicc.scanner.Mark;
import bit.minisys.minicc.scanner.MiniCCScanner;
import bit.minisys.minicc.scanner.Token;
import bit.minisys.minicc.scanner.Type;


import org.jdom.Document;  
import org.jdom.Element;  
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;  

public class MiniCCParser implements IMiniCCParser{
	private static String FILE_NAME;
	private static String OFILE_NAME;
	private int cur;
	private ArrayList<Token> tokens;
	private Element root;
	@Override
	public void run(String iFile, String oFile) throws IOException {
		// TODO Auto-generated method stub
		System.out.println(iFile);
		FILE_NAME = iFile;
		OFILE_NAME = oFile;
		init();
		parseStart(root);
		output();
		System.out.println("parse finish");
	}
	public void init() {
		cur = 0;
		tokens = new ArrayList<Token>();
		readXMLContent();
		root = new Element("ParserTree").setAttribute("name", OFILE_NAME);
	}
	//control function
	public void next() {
		cur++;
		//TODO: maybe need to fix this logical
		if(cur >= tokens.size()) {
			cur --;
		}

	}

	//helper function
	public boolean checkType(String val) {
		if(val.equals("int")) {
			return true;
		}
		if(val.equals("float")) {
			return true;
		}
		if(val.equals("double")) {
			return true;
		}
		if(val.equals("char")) {
			return true;
		}
		return false;
 	}
	public boolean checkFuncType(String val) {
		if (checkType(val)) {
			return true;
		}
		if(val.equals("void")) {
			return true;
		}
		return false;
	}
	//token function
	public boolean id(Element e) {
		Grammar g =  Grammar.IDENTIFIER;
		Element ne = new Element(String.valueOf(g));
		
		Token t = tokens.get(cur);
		if(t.getType().equals(Type.IDENTIFIER)) {
			next();
			ne.setText(t.getValue());
			return succ(e, ne);
		}
		return fail();
	}
	public boolean eq(Element e) {
		Grammar g =  Grammar.OPERATOR;
		Element ne = new Element(String.valueOf(g));
		
		Token t = tokens.get(cur);
		if(t.getValue().equals("=")) {
			next();
			ne.setText(t.getValue());
			return succ(e, ne);
		}
		return fail();
	}
	public boolean constant(Element e) {
		Grammar g =  Grammar.CONSTANT;
		Element ne = new Element(String.valueOf(g));
		
		Token t = tokens.get(cur);
		if(t.getType().equals(Type.CONSTANT) ) {
			next();
			ne.setText(t.getValue());
			return succ(e, ne);
		}
		return fail();
	}
	public boolean lp(Element e) {
		Grammar g =  Grammar.SEPARATOR;
		Element ne = new Element(String.valueOf(g));
		
		Token t = tokens.get(cur);
		if(t.getValue().equals("(")) {
			next();
			ne.setText(t.getValue());
			return succ(e, ne);
		}
		return fail();
	}
	public boolean rp(Element e) {
		Grammar g =  Grammar.SEPARATOR;
		Element ne = new Element(String.valueOf(g));
		
		Token t = tokens.get(cur);
		if(t.getValue().equals(")")) {
			next();
			ne.setText(t.getValue());
			return succ(e, ne);
		}
		return fail();
	}
	public boolean rwhile(Element e) {
		Grammar g =  Grammar.KEYWORD;
		Element ne = new Element(String.valueOf(g));
		
		Token t = tokens.get(cur);
		if(t.getValue().equals("while")) {
			next();
			ne.setText(t.getValue());
			return succ(e, ne);
		}
		return fail();
	}
	public boolean rif(Element e) {
		Grammar g =  Grammar.KEYWORD;
		Element ne = new Element(String.valueOf(g));
		
		Token t = tokens.get(cur);
		if(t.getValue().equals("if")) {
			next();
			ne.setText(t.getValue());
			return succ(e, ne);
		}
		return fail();
	}
	public boolean relse(Element e) {
		Grammar g =  Grammar.KEYWORD;
		Element ne = new Element(String.valueOf(g));
		
		Token t = tokens.get(cur);
		if(t.getValue().equals("else")) {
			next();
			ne.setText(t.getValue());
			return succ(e, ne);
		}
		return fail();
	}
	public boolean lbracket(Element e) {
		Grammar g =  Grammar.SEPARATOR;
		Element ne = new Element(String.valueOf(g));
		
		Token t = tokens.get(cur);
		if(t.getValue().equals("{")) {
			next();
			ne.setText(t.getValue());
			return succ(e, ne);
		}
		return fail();
	}
	public boolean rbracket(Element e) {
		Grammar g =  Grammar.SEPARATOR;
		Element ne = new Element(String.valueOf(g));
		
		Token t = tokens.get(cur);
		if(t.getValue().equals("}")) {
			next();
			ne.setText(t.getValue());
			return succ(e, ne);
		}
		return fail();
	}
	public boolean semicolon(Element e) {
		Grammar g =  Grammar.SEPARATOR;
		Element ne = new Element(String.valueOf(g));
		
		Token t = tokens.get(cur);
		if(t.getValue().equals(";")) {
			next();
			ne.setText(t.getValue());
			return succ(e, ne);
		}
		return fail();
	}
	public boolean rReturn(Element e) {
		Grammar g =  Grammar.KEYWORD;
		Element ne = new Element(String.valueOf(g));
		
		Token t = tokens.get(cur);
		if(t.getValue().equals("return")) {
			next();
			ne.setText(t.getValue());
			return succ(e, ne);
		}
		return fail();
	}
	public boolean judgeOp(Element e) {
		//TODO: change to judge operator
		Grammar g =  Grammar.JU_OPERATOR;
		Element ne = new Element(String.valueOf(g));
		
		Token t = tokens.get(cur);
		if(t.getType().equals(Type.OPERATOR)) {
			next();
			ne.setText(t.getValue());
			return succ(e, ne);
		}
		return fail();
	}
	public boolean comma(Element e) {
		Grammar g =  Grammar.SEPARATOR;
		Element ne = new Element(String.valueOf(g));
		
		Token t = tokens.get(cur);
		if(t.getValue().equals(",")) {
			next();
			ne.setText(t.getValue());
			return succ(e, ne);
		}
		return fail();
	}
	public boolean rOperator(Element e) {
		Grammar g =  Grammar.OPERATOR;
		Element ne = new Element(String.valueOf(g));
		
		Token t = tokens.get(cur);
		if(t.getType().equals(Type.OPERATOR)) {
			next();
			ne.setText(t.getValue());
			return succ(e, ne);
		}
		return fail();
	}
	public boolean rType(Element e) {
		Grammar g =  Grammar.KEYWORD;
		Element ne = new Element(String.valueOf(g));
		
		Token t = tokens.get(cur);
		if(t.getType().equals(Type.KEYWORD)) {
			next();
			ne.setText(t.getValue());
			return succ(e, ne);
		}
		return fail();
	}
	//check method, which will not move cursor
	public boolean isEof() {
		Token t = tokens.get(cur);
		if(t.getValue().equals("#")) {
			return true;
		}
		return false;
	}
	public boolean isId() {
		Token t = tokens.get(cur);
		if(t.getType().equals(Type.IDENTIFIER)) {
			return true;
		}
		return false;
	}
	public boolean isType() {
		Token t = tokens.get(cur);
		if(checkType(t.getValue())) {
			return true;
		}
		return false;
	}
	public boolean isFuncType() {
		Token t = tokens.get(cur);
		if(checkFuncType(t.getValue())) {
			return true;
		}
		return false;
	}
		
	public boolean isRp() {

		Token t = tokens.get(cur);
		if(t.getValue().equals(")") ){
			return true;
		}
		return false;
	}
	public boolean isRb() {
		Token t = tokens.get(cur);
		if(t.getValue().equals("}")) {
			return true;
		}
		return false;
	}
	public boolean isReturn() {
		Token t = tokens.get(cur);
		if(t.getValue().equals("return")) {
			return true;
		}
		return false;
	}
	public boolean isWhile() {
		Token t = tokens.get(cur);
		if(t.getValue().equals("while")) {
			return true;
		}
		return false;
	}
	public boolean isIf() {
		Token t = tokens.get(cur);
		if(t.getValue().equals("if")) {
			return true;
		}
		return false;
	}
	public boolean isElse() {
		Token t = tokens.get(cur);
		if(t.getValue().equals("else")) {
			return true;
		}
		return false;
	}
	public boolean isEq() {
		Token t = tokens.get(cur);
		if(t.getValue().equals("=")) {
			return true;
		}
		return false;
	}
	public boolean isLp() {
		Token t = tokens.get(cur);
		if(t.getValue().equals("(")) {
			return true;
		}
		return false;
	}
	public boolean isNumber() {
		Token t = tokens.get(cur);
		if(t.getType().equals(Type.CONSTANT)) {
			return true;
		}
		return false;
	}
	public boolean isComma() {
		Token t = tokens.get(cur);
		if(t.getValue().equals(",")) {
			return true;
		}
		return false;
	}
	//parse function
	public boolean pushNull(Element e, Element ne) {
		
		e.addContent(ne);
		Element none = new Element("NONE");
		ne.addContent(none);
		return true;
	}
	public boolean fail() {
		Token token = tokens.get(cur);
		int num = token.getNumber();
		System.err.println("unexpected " + token.getValue() + " at line  " + token.getLine() );
		return false;
	}
	public boolean fail(String message) {
		Token token = tokens.get(cur);
		int num = token.getNumber();
		System.err.println(message + " unexpected " + token.getValue() +" at line  " + token.getLine() );
		return false;
	}
	public boolean succ(Element e, Element ne) {
		e.addContent(ne);
		return true;
	}
	public boolean parseStart(Element  e) {
		Grammar g =  Grammar.PROGRAM;
		Element ne = new Element(String.valueOf(g));
		
		if(!funcList(ne)){
			return fail();
		}
		return succ(e, ne);
	}
	public boolean funcList(Element e) {
		Grammar g =  Grammar.FUNCTIONS;
		Element ne = new Element(String.valueOf(g));
	
		if(funcDef(ne)) {
			//check if function list empty
			if(isEof()) {
				return pushNull(e, ne);
			} 
			if(!funcList(ne)){
				return fail();
			} 
			return succ(e, ne);
		} else {
			return fail();
		}
		
	}
	public boolean funcDef(Element e) {
		Grammar g =  Grammar.FUNCTION;
		Element ne = new Element(String.valueOf(g));

		if(funcTypeSpec(ne)) {
			if(id(ne)) {
				if(lp(ne)) {
					if (paraList(ne)) {
						if(rp(ne)) {
							if(lbracket(ne)) {
								if(stmtList(ne)) {
									if(rbracket(ne)) {
										return succ(e, ne);
									}
								}
							}
						}
					}
				}
			}
		}
		return fail();
	}
	public boolean typeSpec(Element e) {
		Grammar g =  Grammar.TYPE;
		Element ne = new Element(String.valueOf(g));
		if(isType()) {
			rType(ne);
			
			return succ(e, ne);
		} else {
			return fail();
		}
	}
	public boolean funcTypeSpec(Element e) {
		Grammar g =  Grammar.FUNC_TYPE;
		Element ne = new Element(String.valueOf(g));
		if(isFuncType()) {
			rType(ne);
			
			return succ(e, ne);
		} else {
			return fail();
		}
	}
	public boolean paraList(Element e) {
		Grammar g =  Grammar.PARAS;
		Element ne = new Element(String.valueOf(g));
		
		if(isRp()) {
			return pushNull(e, ne);
		}
		if(typeSpec(ne)) {
			if(id(ne)) {
				//judge by fist and follow
				if(isRp()) {
					return pushNull(e, ne);
				} else {
					if(comma(ne)) {
						if(paraList(ne)) {
							return succ(e, ne);
						}
					}
				}
			}
		}
		return fail();
		
		
	}
	public boolean idList(Element e) {
		Grammar g =  Grammar.IDENTIFIERS;
		Element ne = new Element(String.valueOf(g));
		if(isComma()) {
			if(comma(ne)) {
				if(id(ne)) {
					if(idList(ne)) {
						return succ(e, ne);
					}
				}
			}
		} else {
			return pushNull(e, ne);
		}
		return fail();
	}

	
	public boolean stmtList(Element e) {
		Grammar g =  Grammar.STMTS;
		Element ne = new Element(String.valueOf(g));
		
		if(isRb()) {
			return pushNull(e, ne);
		}
		if(stmt(ne)) {
			if(stmtList(ne)) {
				return succ(e, ne);
			} 
		}
		return fail();
	}
	public boolean stmt(Element e) {
		Grammar g =  Grammar.STMT;
		Element ne = new Element(String.valueOf(g));
		
		if(isReturn()) {
			if(rtnStmt(ne)) {
				return succ(e, ne);
			}
		} else if(isWhile()) {
			if(whileStmt(ne)) {
				return succ(e, ne);
			}
		} else if(isIf()) {
			if(branchStmt(ne)) {
				return succ(e, ne);
			}
		} else if(isId()) {
			if(assignStmt(ne)) {
				return succ(e, ne);
			}
		} else if(isType()) {
			//here move forward to see next
			cur++;
			if(isId()) {
				//here move forward to see next
				cur++;
				if(isEq()) {
					cur --;
					cur --;
					if(assignStmt(ne)) {
						return succ(e, ne);
					}
				} else {
					cur --;
					cur --;
					if(declareStmt(ne)) {
						return succ(e, ne);
					}
				}
			} else {
				cur --;
			}
		}
		
		//a little trick here, do not judge its first, because if it is not assign, it will fail for sure

		return fail();
		
 	}
	public boolean declareStmt(Element e) {
		Grammar g =  Grammar.DECLARE_STMT;
		Element ne = new Element(String.valueOf(g));
		if(typeSpec(ne)) {
			if(id(ne)) {
				if(idList(ne)) {
					if(semicolon(ne)) {
						return succ(e, ne);
					}
				}
			}
		}
		return fail();
	}
	public boolean rtnStmt(Element e) {
		Grammar g =  Grammar.RTN_STMT;
		Element ne = new Element(String.valueOf(g));
		if(rReturn(ne)) {
			if(expr(ne)) {
				if(semicolon(ne)) {
					return succ(e, ne);
				}
			}
		}
		return fail();
	}
	//TODO: abstract the same code out
	public boolean assignStmt(Element e) {
		Grammar g =  Grammar.ASSIGN_STMT;
		Element ne = new Element(String.valueOf(g));
		if(isType()) {
			if(typeSpec(ne)) {
				if(id(ne)) {
					if(eq(ne)) {
						if(expr(ne)) {
							if(semicolon(ne)) {
								return succ(e, ne);
							}
						}
					}
				}
			} 
		}
		
		if(id(ne)) {
			if(eq(ne)) {
				if(expr(ne)) {
					if(semicolon(ne)) {
						return succ(e, ne);
					}
				}
			}
		}
			
			
		
		return fail();
	}
	
	public boolean whileStmt(Element e) {
		Grammar g =  Grammar.WHILE_STMT;
		Element ne = new Element(String.valueOf(g));
		if(rwhile(ne)) {
			if(lp(ne)) {
				if(judgeStmt(ne)) {
					if(rp(ne)) {
						if(lbracket(ne)) {
							if(stmtList(ne)) {
								if(rbracket(ne)) {
									return succ(e, ne);
								}
							}
						}
					}
				}
			}
		}
		return fail();
	}
	
	public boolean branchStmt(Element e) {
		Grammar g =  Grammar.BRANCH_STMT;
		Element ne = new Element(String.valueOf(g));
		if(rif(ne)) {
			if(lp(ne)){
				if(judgeStmt(ne)) {
					if(rp(ne)) {
						if(lbracket(ne)) {
							if(stmtList(ne)) {
								if(rbracket(ne)){
									//judge by first
									if(isElse()) {
										if(relse(ne)) {
											if(lbracket(ne)) {
												if(stmtList(ne)) {
													if(rbracket(ne)) {
														return succ(e, ne);
													}
												}
											}
											
										}
									} else {
										return pushNull(e, ne);
									}
								}
							}
						}
					}
				}
			}
		}
		return fail();
	}
	public boolean judgeStmt(Element e) {
		Grammar g =  Grammar.JU_STMT;
		Element ne = new Element(String.valueOf(g));
		if(factor(ne)) {
			//judge by follow
			if(isRb()) {
				return pushNull(e, ne);
			}
			if(judgeOp(ne)) {
				if(factor(ne)) {
					return succ(e, ne);
				}
			} 
		}
		return fail();
	}
	public boolean expr(Element e) {
		Grammar g =  Grammar.EXPR;
		Element ne = new Element(String.valueOf(g));
		
	
		if(term(ne)) {
			if(expr2(ne)) {
				return succ(e, ne);
			}
		}
		return fail();
	}
	public boolean expr2(Element e) {
		Grammar g =  Grammar.EXPR2;
		Element ne = new Element(String.valueOf(g));
		
		String val = tokens.get(cur).getValue();
		if(val.equals("+") || val.equals("-") ) {
			rOperator(ne);
			
			if(term(ne)) {
				if(expr2(ne)) {
					return succ(e, ne);
				}
			} 
			return fail();
		}
		return pushNull(e, ne);	
	}
	public boolean term(Element e) {
		Grammar g =  Grammar.TERM;
		Element ne = new Element(String.valueOf(g));
	
		if(factor(ne)) {
			if(term2(ne)) {
				return succ(e, ne);
			}
			
		}
		return fail();
	}
	public boolean term2(Element e) {
		Grammar g =  Grammar.TERM2;
		Element ne = new Element(String.valueOf(g));

		String val = tokens.get(cur).getValue();
		if(val.equals("*")||val.equals("/")) {
			rOperator(ne);

			if(factor(ne)) {
				if (term2(ne)) {
					return succ(e, ne);
				}
			}
			
			return fail();

		} else {
			return pushNull(e, ne);
		}
	}
	public boolean factor(Element e) {
		Grammar g =  Grammar.FACTOR;
		Element ne = new Element(String.valueOf(g));
		
		if(isLp()) {
			if(lp(ne)) {
				if(expr(ne)) {
					if(rp(ne)) {
						return succ(e, ne);
					}
				}
			}
		}
		if(isNumber()) {
			if(constant(ne)) {
				return succ(e, ne);
			}
		}
		if(id(ne)) {//if not id, will fail
			return succ(e, ne);
		}
		
		return fail();
	}
	
	//input function
	public void readXMLContent() {
	    SAXBuilder builder = new SAXBuilder();
	    try {
	        Document doc = builder.build(new File(FILE_NAME));
	        Element root = doc.getRootElement();
	        Element rootEl = root.getChild("tokens");
	        //获得所有子元素
	        List<Element> list = rootEl.getChildren();
	        for (Element el : list) {
	        	Token token = new Token();
	        	
	            token.setNumber(Integer.parseInt(el.getChildText("number")));
	            
	            String mark = el.getChildText("mark").toUpperCase();
	            if(!mark.equals("NULL")) {
	            	token.setMark(Mark.valueOf(mark));
	            }
	            
	            String type = el.getChildText("type").toUpperCase();
	            if(type.equals("#")) {
	            	token.setType(Type.END);
	            } else if(!type.equals("NULL")) {
	            	token.setType(Type.valueOf(type));
	            }
	            
	            token.setValue(el.getChildText("value"));
	            token.setLine(Integer.parseInt(el.getChildText("line")));
	            
	            tokens.add(token);
	        }
	    } catch (JDOMException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	//output function
	private void output() {
		Document doc = new Document(root);
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
		
		MiniCCScanner scanner = new MiniCCScanner();
		try {
			scanner.run("test.pp.c", "test.token.xml") ;
		} catch (IOException e) {
			e.printStackTrace();
		}
		MiniCCParser parser = new MiniCCParser();
		try {
			parser.run("test.token.xml", "test.tree.xml") ;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
