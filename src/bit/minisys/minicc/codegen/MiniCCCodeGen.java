package bit.minisys.minicc.codegen;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.xml.sax.SAXException;

import bit.minisys.minicc.parser.Grammar;
import bit.minisys.minicc.parser.MiniCCParser;
import bit.minisys.minicc.scanner.Mark;
import bit.minisys.minicc.scanner.MiniCCScanner;
import bit.minisys.minicc.scanner.Token;
import bit.minisys.minicc.scanner.Type;

public class MiniCCCodeGen implements IMiniCCCodeGen {
	private static String FILE_NAME;
	private static String OFILE_NAME;
	
	
	private int[] tempReg;
	private boolean[] isTempRegFree;
	
	private int[] paraReg;
	private boolean[] isParaRegFree;
 	
	private Hashtable<String, Entry> varTable;
	private ArrayList<FuncEntry> funcTable;
	
	private FuncEntry currentFuncEntry;
	
	private int labelNum;
	private int ifcount;
	private int whilecount;
	//for expression traverse
	private int exprRegNum;
	private boolean isInstant;
	private int instant;
	private Reg rightReg;
	
	private Element newRoot;
	
	private String code;
	
	@Override
	public void run(String iFile, String oFile) throws IOException, ParserConfigurationException, SAXException {
		// TODO Auto-generated method stub
		//System.out.println(iFile);
		FILE_NAME = iFile;
		OFILE_NAME = oFile;
		init();
		input();
		traverseAST();
		output();
		
		
	}
	public void init() {
		tempReg = new int[10];
		isTempRegFree = new boolean[10];
		for (int i = 0; i < isTempRegFree.length; i++) {
			isTempRegFree[i] = true;
		}
		
		paraReg = new int[4];
		isParaRegFree = new boolean[4];
		for (int i = 0; i < isParaRegFree.length; i++) {
			isParaRegFree[i] = true;
		}
		
		varTable = new Hashtable<String, Entry>();
		funcTable = new ArrayList<FuncEntry>();
		newRoot = new Element("AST");
		code = "\t.data\n\n\t.text\n\n__init:\n\t# setup the base address of stack\n"
				+ "\tlui $sp, 0x8000\n"
				+ "\taddi $sp, $sp, 0x0000\n\n"
				+ "\t# allocate stack frame for main function\n"
				+ "\taddiu $sp, $sp, -64\n"
				+ "\tjal __main\n"
				+ "\t# make a system call and terminate the program\n"
				+ "\tli $v0, 10\n"
				+ "\tsyscall\n"
				+ "\tnop\n\n";
	}
	public void input() {
		 SAXBuilder builder = new SAXBuilder();
		    try {
		        Document doc = builder.build(new File(FILE_NAME));
		        Element root = doc.getRootElement();
		        Element rootEl = root.getChild("PROGRAM");
		        traverseNode(rootEl, newRoot);
		        
		    } catch (JDOMException e) {
		        e.printStackTrace();
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
	}

	//generate AST
	public void traverseNode(Element el, Element parent) {
		Grammar type = Grammar.valueOf(el.getName().toUpperCase());
		//List<Element> children = el.getChildren();
		Element newNode;
		switch(type) {
		case PROGRAM:{
			newNode = makeNode(el, parent);
			Element funcsEl = el.getChild(Grammar.FUNCTIONS.toString());
			traverseNode(funcsEl, newNode);
		}
			break;
		case FUNCTIONS:{
			Element functionEl = el.getChild(Grammar.FUNCTION.toString());
			traverseNode(functionEl, parent);
			Element funcsEl = el.getChild(Grammar.FUNCTIONS.toString());
			if(funcsEl != null && funcsEl.getChild(Grammar.FUNCTION.toString()) != null) {
				traverseNode(funcsEl, parent);
			}
		}
			break;
		case FUNCTION:{
			newNode = makeNode(el, parent);
			Element funcTypeEl = el.getChild(Grammar.FUNC_TYPE.toString()).getChild(Grammar.KEYWORD.toString());
			makeNode(Grammar.FUNC_TYPE.toString(), funcTypeEl.getText(), newNode);
			
			Element id = el.getChild(Grammar.IDENTIFIER.toString());
			makeNode(id, newNode);
			
			Element paraList = el.getChild(Grammar.PARAS.toString());
			traverseNode(paraList, newNode);
			
			Element stmtList = el.getChild(Grammar.STMTS.toString());
			traverseNode(stmtList, newNode);
		}
			break;
		case PARAS:{
			if(el.getChild(Grammar.TYPE.toString()) == null) {
		    	break;
		    }
			
			//add a paraNode so can make code generator easily
			Element para = new Element(Grammar.PARA.toString());
		    Element paraNode = makeNode(para, parent);
			
		    
			Element typeEl = el.getChild(Grammar.TYPE.toString()).getChild(Grammar.KEYWORD.toString());
			makeNode(Grammar.TYPE.toString(), typeEl.getText(), paraNode);
			
			Element id = el.getChild(Grammar.IDENTIFIER.toString());
			makeNode(id, paraNode);
			
			Element paraList = el.getChild(Grammar.PARAS.toString());
			if(paraList != null && paraList.getChild(Grammar.IDENTIFIER.toString()) != null) {
				traverseNode(paraList, parent);
			}
			break;
		}
		case STMTS:{
			Element stmt = el.getChild(Grammar.STMT.toString());
			if(stmt != null) {
				traverseNode(stmt, parent);
			}
			Element stmts = el.getChild(Grammar.STMTS.toString());
			if(stmts != null && stmts.getChild(Grammar.STMT.toString()) != null) {
				traverseNode(stmts, parent);
			}
		}
			break;
		case STMT: {
			newNode = makeNode(el, parent);
			Element s = (Element) el.getChildren().get(0);
			traverseNode(s, newNode);
		}
			break;
		case DECLARE_STMT:{
			newNode = makeNode(el, parent);
			Element typeEl = el.getChild(Grammar.TYPE.toString()).getChild(Grammar.KEYWORD.toString());
			makeNode(Grammar.TYPE.toString(), typeEl.getText(), newNode);
			Element idEl = el.getChild(Grammar.IDENTIFIER.toString());
			makeNode(idEl, newNode);
			Element idsEl = el.getChild(Grammar.IDENTIFIERS.toString());
			if(idsEl.getChild(Grammar.IDENTIFIER.toString()) != null) {
				traverseNode(idsEl, newNode);
			}
			break;
		}
		case RTN_STMT:{
			newNode = makeNode(el, parent);
			Element expr = el.getChild(Grammar.EXPR.toString());
			newNode = makeNode(expr, newNode);
			traverseNode(expr, newNode);
			
		}
			break;
		case ASSIGN_STMT : {
			newNode = makeNode(el, parent);
			Element typeEl = el.getChild(Grammar.TYPE.toString());
			if(typeEl != null) {
				makeNode(typeEl.getName(), typeEl.getChild(Grammar.KEYWORD.toString()).getText(), newNode);
			}
			Element id = el.getChild(Grammar.IDENTIFIER.toString());
			makeNode(id, newNode);
			Element expr = el.getChild(Grammar.EXPR.toString());
			newNode = makeNode(expr, newNode);
			traverseNode(expr, newNode);
		}
			break;
		case WHILE_STMT: {
			newNode = makeNode(el, parent);
			Element juStmt = el.getChild(Grammar.JU_STMT.toString());
			traverseNode(juStmt, newNode);
			Element stmts = el.getChild(Grammar.STMTS.toString());
			traverseNode(stmts, newNode);
		}
			break;
		case JU_STMT: {
			newNode = makeNode(el, parent);
			List<Element> factors = el.getChildren(Grammar.FACTOR.toString());
			traverseNode(factors.get(0), newNode);
			Element jdOp = el.getChild(Grammar.JU_OPERATOR.toString());
			makeNode(jdOp, newNode);
			traverseNode(factors.get(1), newNode);
		}
			break;
		case BRANCH_STMT : {
			newNode = makeNode(el, parent);
			List<Element> ifelse = el.getChildren(Grammar.KEYWORD.toString());
			Element juStmt = el.getChild(Grammar.JU_STMT.toString());
			List<Element> stmts = el.getChildren(Grammar.STMTS.toString());
			Element ifel = makeNode("IF", null, newNode);
			traverseNode(juStmt, ifel);
			traverseNode(stmts.get(0), ifel);
			if(ifelse.size() > 1 && ifelse.get(1).getText().equals("else")) {
				//else stmt exits
				Element elseel = makeNode("ELSE", null, newNode);
				traverseNode(stmts.get(1), elseel);
			}
		}
			break;
		case EXPR: {
			Element term = el.getChild(Grammar.TERM.toString());
			Element expr2 = el.getChild(Grammar.EXPR2.toString());
			Element op = expr2.getChild(Grammar.OPERATOR.toString());
			
			Element expr = new Element("EXPR");
			if( op != null) {
				//newNode = makeNode(op, parent);
				//traverseNode(term, newNode);
				//traverseNode(expr2, newNode);
				//newNode = makeNode(Grammar.EXPR.toString(), null, parent);
				
				traverseNode(term, expr);
				traverseNode(expr2, expr);
			} else {
				//newNode = makeNode(Grammar.EXPR.toString(), null, parent);
				traverseNode(term, expr);
			}
			//makeCloneNode(expr, parent);
			newNode = transformExpr(expr);
			//parent.removeContent();
			makeCloneNode(newNode, parent);

		}
			break;
		case EXPR2: {
			Element op = el.getChild(Grammar.OPERATOR.toString());
			newNode = makeNode(op, parent);
			Element term = el.getChild(Grammar.TERM.toString());
			traverseNode(term, parent);
			Element expr2 = el.getChild(Grammar.EXPR2.toString());
			if(expr2 != null && expr2.getChild(Grammar.OPERATOR.toString()) != null) {
				traverseNode(expr2, parent);
			}
		}
			break;
		case TERM: {
			Element factor = el.getChild(Grammar.FACTOR.toString());
			Element term2 = el.getChild(Grammar.TERM2.toString());
			Element op = term2.getChild(Grammar.OPERATOR.toString());
			Element term = new Element("TERM");
			if(op != null) {
				//newNode = makeNode(op, parent);
				//traverseNode(factor, newNode);
				//traverseNode(term2, newNode);
				//newNode = makeNode(Grammar.TERM.toString(), null, parent);
				traverseNode(factor, term);
				traverseNode(term2,  term);
			} else {
				traverseNode(factor, term);
			}
			//makeCloneNode(term, parent);
			
			newNode = transformTerm(term);
			//parent.removeContent();
			makeCloneNode(newNode, parent);
		}
			break;
		case TERM2: {
			Element op = el.getChild(Grammar.OPERATOR.toString());
			newNode = makeNode(op, parent);
			Element factor = el.getChild(Grammar.FACTOR.toString());
			traverseNode(factor, parent);
			Element term2 = el.getChild(Grammar.TERM2.toString());
			if(term2 != null && term2.getChild(Grammar.TERM2.toString()) != null) {
				traverseNode(term2, parent);
			}
		}
			break;
		case FACTOR: {
			Element id = el.getChild(Grammar.IDENTIFIER.toString());
			Element constEl = el.getChild(Grammar.CONSTANT.toString());
			Element expr = el.getChild(Grammar.EXPR.toString());
			if(id != null) {
				makeNode(id, parent);
			} else if(constEl != null){
				makeNode(constEl, parent);
			} else if(expr != null) {
				traverseNode(expr, parent);
			}
		}
			break;
		case IDENTIFIERS:{
			Element idEl = el.getChild(Grammar.IDENTIFIER.toString());
			newNode = makeNode(idEl, parent);
			Element idsEl = el.getChild(Grammar.IDENTIFIERS.toString());
			
			if(idsEl.getChild(Grammar.IDENTIFIER.toString()) != null) {
				traverseNode(idsEl, parent);
			}
			break;
		}
		
		}
	}
	//transform for expression
	public Element transformTerm(Element term) {
			List<Element> s = term.getChildren();
			Element newNode = null;
			int len = s.size();
			if(len >= 3) {
				Element temp = term;
				for(int i = len -2; i >=1; i-=2) {
					temp = makeNode(s.get(i), temp);
					if(i == len -2) {
						newNode = temp;
					}
					makeNode(s.get(i+1), temp);
					
				}
				makeNode(s.get(0), temp);
			} else {
				newNode = s.get(0);
			}
			return newNode;
		}
	public Element transformExpr(Element expr) {
			List<Element> s = expr.getChildren();
			Element newNode = null;
			int len = s.size();
			if(len >= 3) {
				Element temp = expr;
				for(int i = len -2; i >=1; i-=2) {
					temp = makeNode(s.get(i), temp);
					if(i == len -2) {
						newNode = temp;
					}
					makeCloneNode(s.get(i+1), temp);
					
				}
				makeCloneNode(s.get(0), temp);
			} else {
				newNode = s.get(0);
			}
			return newNode;
		}
	//helper method for generate AST
	public Element makeNode(Element node, Element parent) {
		Element newNode = new Element(node.getName());
		//System.out.println(node.getName());
		
		newNode.setText(node.getText());
		parent.addContent(newNode);
		return newNode;
	}
	public Element makeNode(String name, String text, Element parent) {
		Element newNode = new Element(name);
		//System.out.println(name);
		newNode.setText(text);
		parent.addContent(newNode);
		return newNode;
	}
	public Element makeCloneNode(Element node, Element parent) {
		Element newNode = (Element) node.clone();
		parent.addContent(newNode);
		return newNode;
	}
	
	//code generate
	public void traverseAST() {
		System.out.println("start traverse AST");
		List<Element> funcList = newRoot.getChild(Grammar.PROGRAM.toString()).getChildren();
		for(Element func: funcList) {
			
			String funcType = func.getChildText(Grammar.FUNC_TYPE.toString());
			String funcName = func.getChildText(Grammar.IDENTIFIER.toString());
			currentFuncEntry = new FuncEntry(funcName, funcType);
			
			code += "__" + funcName + ":\n";
			
			List<Element> paras = func.getChildren(Grammar.PARA.toString());
			for(int i = 0; i < paras.size(); i++) {
				Element para = paras.get(i);
				String varName = para.getChild(Grammar.IDENTIFIER.toString()).getText();
				getParaReg(varName);
			}
			List<Element> stmts = func.getChildren(Grammar.STMT.toString());
			for(Element stmt: stmts) {
				traverseStmt(stmt);
			}
			//the code below is for function call expression and global var
			//entry.setVarTable(varTable);
			//funcTable.add(entry);
			changeFunc();
		}
	}
	public void changeFunc() {
		for (int i = 0; i < isTempRegFree.length; i++) {
			isTempRegFree[i] = true;;
		}
		for (int i = 0; i < isParaRegFree.length; i++) {
			isParaRegFree[i] = true;;
		}
		varTable.clear();
		
		if(!currentFuncEntry.funcType.equals("void")) {
			//TODO:deal with return type
		}
		code += "\tjr $ra\n";
	}
	public void traverseStmt(Element parent) {
		Element stmt = (Element) parent.getChildren().get(0);
		Grammar type =  Grammar.valueOf(stmt.getName());
		switch(type) {
		case DECLARE_STMT: {
			List<Element> idEl = stmt.getChildren(Grammar.IDENTIFIER.toString());
			for(Element el: idEl) {
				String varName = el.getText();
				//System.out.println("var " + varName);
				getTempReg(varName);
			}
		}
		break;
		case ASSIGN_STMT: {
			String left = stmt.getChildText(Grammar.IDENTIFIER.toString());
			Element typeEl = stmt.getChild(Grammar.TYPE.toString());
			Reg leftReg = null;
			Reg rightReg = null;
			if(typeEl != null) {
				leftReg = getTempReg(left);
			} else {
				leftReg = findReg(left);
			}
			
			Element expr = stmt.getChild(Grammar.EXPR.toString());
			Element op = expr.getChild(Grammar.OPERATOR.toString());
			if(op == null) {
				Element constant = expr.getChild(Grammar.CONSTANT.toString());
				if(constant != null) {
					rightReg = genLi(Integer.valueOf(constant.getText()));
				} else {
					Element id = expr.getChild(Grammar.IDENTIFIER.toString());
					rightReg = findReg(id.getText());
				}
			} else {
				rightReg = traverseExpr(op);
			}
			code += "\tmove "+ leftReg.toString() + " " +  rightReg.toString() + "\n";
		}
		break;
		case RTN_STMT: {
			Element expr = stmt.getChild(Grammar.EXPR.toString());
			Element op = expr.getChild(Grammar.OPERATOR.toString());
			
			if(op == null) {
				Element constant = expr.getChild(Grammar.CONSTANT.toString());
				if(constant != null) {
					rightReg = genLi(Integer.valueOf(constant.getText()));
				} else {
					Element id = expr.getChild(Grammar.IDENTIFIER.toString());
					rightReg = findReg(id.getText());
				}
			} else {
				rightReg = traverseExpr(op);	
			}
			Reg leftReg = new Reg(RegType.V, 0);
			code += "\tmove "+ leftReg.toString() + " " +  rightReg.toString() + "\n";
		}
		break;
		case WHILE_STMT: {
			whilecount++;
			Element juStmt = stmt.getChild(Grammar.JU_STMT.toString());
			Element factor1 = (Element) juStmt.getChildren().get(0);
			Element factor2 = (Element) juStmt.getChildren().get(2);
			Element juOp = (Element)juStmt.getChild(Grammar.JU_OPERATOR.toString());
			
			String startLabel = "_while_" + whilecount + "_start";
			code += startLabel + ":\n";
			String outLabel =  "_while_" + whilecount + "_out";
			
			Reg leftReg = null;
			Reg rightReg = null;
			if(factor1.getName().equals(Grammar.CONSTANT.toString())) {
				leftReg = genLi(Integer.valueOf(factor1.getText()));
			} else {
				leftReg = findReg(factor1.getText());
			}
			if(factor2.getName().equals(Grammar.CONSTANT.toString())) {
				rightReg = genLi(Integer.valueOf(factor2.getText()));
			} else {
				rightReg = findReg(factor2.getText());
			}
			
			//TODO: zip back technology should be used here
			String target = outLabel;
			char[] ju = juOp.getText().toCharArray();
			switch(ju[0]) {
			case '>':
				if(ju.length > 1) {
					code += "\tblt " + leftReg.toString() + ", " + rightReg.toString() + ", " + target + "\n";
				} else {
					code += "\tble " + leftReg.toString() + ", " + rightReg.toString() + ", " + target + "\n";
				}
				break;
			case '<':
				if(ju.length > 1) {
					code += "\tbgt " + leftReg.toString() + ", " + rightReg.toString() + ", " + target + "\n";
				} else {
					code += "\tbge " + leftReg.toString() + ", " + rightReg.toString() + ", " + target + "\n";
				}
				break;
			case '=':
				code += "\tbne " + leftReg.toString() + ", " + rightReg.toString() + ", " + target + "\n";
				break;
			}
			List<Element> stmts = stmt.getChildren(Grammar.STMT.toString());
			for(Element s: stmts) {
				traverseStmt(s);
			}
			//TODO: zip back technology should be used here
			code += "\tj " + startLabel + "\n";
			code += outLabel + ":\n";
			
		}
		break;
		case BRANCH_STMT: {
			ifcount++;
			boolean hasElse = false;
			Element ifEl = stmt.getChild("IF");
			Element elseEl = stmt.getChild("ELSE");
			if(elseEl != null){
				hasElse = true;
			}
			Element juStmt = ifEl.getChild(Grammar.JU_STMT.toString());
			Element factor1 = (Element) juStmt.getChildren().get(0);
			Element factor2 = (Element) juStmt.getChildren().get(2);
			Element juOp = (Element)juStmt.getChild(Grammar.JU_OPERATOR.toString());
			
			Reg leftReg = null;
			Reg rightReg = null;
			if(factor1.getName().equals(Grammar.CONSTANT.toString())) {
				leftReg = genLi(Integer.valueOf(factor1.getText()));
			} else {
				leftReg = findReg(factor1.getText());
			}
			if(factor2.getName().equals(Grammar.CONSTANT.toString())) {
				rightReg = genLi(Integer.valueOf(factor2.getText()));
			} else {
				rightReg = findReg(factor2.getText());
			}
			
			//TODO: zip back technology should be used here
			String target, outLabel;
			outLabel = "_if_" + ifcount + "_out";
			if(hasElse) {
				target = "_if_" + ifcount+"_else";
			} else {
				target = "_if_" + ifcount;
			}
			char[] ju = juOp.getText().toCharArray();
			switch(ju[0]) {
			case '>':
				if(ju.length > 1) {
					code += "\tblt " + leftReg.toString() + ", " + rightReg.toString() + ", " + target + "\n";
				} else {
					code += "\tble " + leftReg.toString() + ", " + rightReg.toString() + ", " + target + "\n";
				}
				break;
			case '<':
				if(ju.length > 1) {
					code += "\tbgt " + leftReg.toString() + ", " + rightReg.toString() + ", " + target + "\n";
				} else {
					code += "\tbge " + leftReg.toString() + ", " + rightReg.toString() + ", " + target + "\n";
				}
				break;
			case '=':
				code += "\tbne " + leftReg.toString() + ", " + rightReg.toString() + ", " + target + "\n";
				break;
			}
			List<Element> stmts = ifEl.getChildren(Grammar.STMT.toString());
			for(Element s: stmts) {
				traverseStmt(s);
			}
			if(hasElse) {
				code += "\tj " + outLabel + '\n';
			}
			code +=  target + ":\n";
			if(hasElse) {
				List<Element> ss = elseEl.getChildren(Grammar.STMT.toString());
				for(Element s: ss) {
					traverseStmt(s);
				}
				code +=  outLabel + ":\n";
			}
			
		}
		break;
			
		}
	}
	public Reg findReg(String varName) {
		boolean exist = varTable.containsKey(varName);
		if(!exist) {
			System.err.println("not define symbol " + varName);
			System.exit(1);
		}
		Entry entry = varTable.get(varName);
		
		Reg reg = new Reg(entry.regType, entry.regNum);
		return reg;
	}
	
	
	public Reg traverseExpr(Element op) {
		//Element op = expr.getChild(Grammar.OPERATOR.toString());
		Reg leftReg = null;
		Reg rightReg = null;
		Reg ansReg = null;
		
			Element left = (Element) op.getChildren().get(1);
			Element right = (Element) op.getChildren().get(0);
			
			if(left.getName().equals(Grammar.OPERATOR.toString())) {
				leftReg = traverseExpr(left);
			} else {
				if(left.getName().equals(Grammar.CONSTANT.toString())) {
					leftReg = genLi(Integer.valueOf(left.getText()));
				} else {
					leftReg = findReg(left.getText());
				}
			}
			
			if(right.getName().equals(Grammar.OPERATOR.toString())) {
				rightReg = traverseExpr(right);
			} else {
				if(right.getName().equals(Grammar.CONSTANT.toString())) {
					rightReg = genLi(Integer.valueOf(right.getText()));
				} else {
					rightReg = findReg(right.getText());
				}
			}
			
			switch(op.getText().toCharArray()[0]) {
			case '+' :
				ansReg = genAdd(leftReg, rightReg);
				break;
			case '-':
				ansReg = genMinus(leftReg, rightReg);
				break;
			case '*':
				ansReg = genMul(leftReg, rightReg);
				break;
			case '/':
				ansReg = genDiv(leftReg, rightReg);
				break;
			}
		
		
		return ansReg;
	}
	
	//code generator
	public Reg genLi(int num) {
		Reg left = getTempReg(num);
		code += "\tli " + left.toString() + ", " + num + '\n';
		return left;
	}
	public Reg genAdd(Reg left, Reg right) {
		Reg ans = getTempReg();
		code += "\tadd " + ans.toString() + ", " + left.toString() + ", " + right.toString() + "\n";
		return ans;
	}
	public Reg genMinus(Reg left, Reg right) {
		code += "\tsub " + left.toString() + ", " + right.toString() + "\n";
		return left;
	}
	public Reg genMul(Reg left, Reg right) {
		Reg ans = getTempReg();
		code += "\tmul " + ans.toString() + ", " + left.toString() + ", " + right.toString() + "\n";
		return ans;
	}
	public Reg genDiv(Reg left, Reg right) {
		Reg ans = getTempReg();
		code += "\tdiv " + left.toString() + ", " + right.toString() + "\n";
		code += "\tmflo " + ans.toString() + '\n';
		return ans;
	}
	//output part
	public String genLabel() {
		String label = "label" + labelNum++;
		code += label +  ":\n";
		return label;
	}
	public void output() {
		//output the AST
		Document doc = new Document(newRoot);
		 Format format = Format.getPrettyFormat();  
	     XMLOutputter XMLOut = new XMLOutputter(format);  
	     try {
	    	 //here should be OFILE NAME cat to name.l
	    	 XMLOut.output(doc, new FileOutputStream(OFILE_NAME));  
	     } catch (IOException e){
	    	 e.printStackTrace();
	     }
	     //output the code
	     try {
	            File file = new File("test.code.s");
	            PrintStream ps = new PrintStream(new FileOutputStream(file));
	            ps.print(code);
	        } catch (FileNotFoundException e) {
	            e.printStackTrace();
	        }
	}
	
	//register assign part
	public void saveReg(Entry entry) {
		//TODO:save to memory
	}
	public Reg getTempReg(String varName) {
		boolean hasFree = false;
		for(int i = 0; i <= tempReg.length; i++) {
			if(isTempRegFree[i]) {
				hasFree = true;
				isTempRegFree[i] = false;
				varTable.put(varName, new Entry(varName, RegType.T, i));
				return new Reg(RegType.T, i);
			} 
		}
		//TODO: should use LRU strategy to save
		if(!hasFree) {
			saveReg(varTable.get(0));
			varTable.remove(0);
			return new Reg(RegType.T, 0);
		}

		return null;
	}
	public Reg getTempReg(int num) {
		boolean hasFree = false;
		for(int i = 0; i < tempReg.length; i++) {
			if(isTempRegFree[i]) {
				hasFree = true;
				isTempRegFree[i] = false;
				return new Reg(RegType.T, i);
			} 
		}
		//TODO: should use LRU strategy to save
		if(!hasFree) {
			saveReg(varTable.get(0));
			varTable.remove(0);
			return new Reg(RegType.T, 0);
		}

		return null;
	}
	public Reg getTempReg() {
		boolean hasFree = false;
		for(int i = 0; i < tempReg.length; i++) {
			if(isTempRegFree[i]) {
				hasFree = true;
				isTempRegFree[i] = false;
				return new Reg(RegType.T, i);
			} 
		}
		//TODO: should use LRU strategy to save
		if(!hasFree) {
			saveReg(varTable.get(0));
			varTable.remove(0);
			return new Reg(RegType.T, 0);
		}

		return null;
	}
	public Reg getParaReg(String varName) {
		boolean hasFree = false;
		for(int i = 0; i < paraReg.length; i++) {
			if(isParaRegFree[i]) {
				hasFree = true;
				isParaRegFree[i] = false;
				varTable.put(varName, new Entry(varName, RegType.A, i));
				return new Reg(RegType.A, i);
			}
		}
		if(!hasFree) {
			//TODO: here should be load from memory
			saveReg(varTable.get(0));
			varTable.remove(0);
			return new Reg(RegType.A, 0);
		}
		return null;
	}
public static void main(String args[]) throws ParserConfigurationException, SAXException {
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
		MiniCCCodeGen generator = new MiniCCCodeGen();
		
		try {
			generator.run("test.tree.xml", "test.ast.xml") ;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
