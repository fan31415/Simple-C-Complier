package bit.minisys.minicc.codegen;

public class Entry {
	public String varName;
	public RegType regType;
	public int regNum;
	public String getRegName() {
		return regType.toString() + regNum;
	}
	public Entry(String varName, RegType regType, int regNum) {
		this.varName = varName;
		this.regType = regType;
		this.regNum = regNum;
	}
}
