package bit.minisys.minicc.codegen;

import java.util.ArrayList;
import java.util.List;

public class FuncEntry {
	public String funcName;
	public String funcType;
	private ArrayList<Entry> varTable;
	public FuncEntry(String funcName, String funcType) {
		this.funcName = funcName;
		this.funcType = funcType;
	}
	public void setVarTable(ArrayList<Entry> varTable) {
		this.varTable = varTable;
	}
	public ArrayList<Entry> getVarTable() {
		return varTable;
	}
}
