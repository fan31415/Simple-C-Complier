package bit.minisys.minicc.scanner;

public class Constants {
	//will not use, just for temporary get their hash value before compile
	// keyword
	 public static final String keyword[]={"auto", "break", "case", "char", "const", "continue",
			 "default", "do", "double", "else", "enum", "extern", "float", 
			 "for", "goto", "if", "incline", "int","long", "register", "restrict", 
			 "return", "short", "signed", "sizeof", "static", "struct", "switch",
			 	"typedef", "union", "unsigned", "void", "volatitle", "while", "_Alignas", "_Alignof", 
			 	"_Atomic", "_Bool", "_Complex", "_Generic", "_Imaginary", "_Noreturn", "_Static_assert",
			 	"_Thread_local"};
	 //hash of keyword
	 public static final int hashKeyword[] = {
			3005871,94001407,3046192,3052374,94844771,-567202649,1544803905,3211,
			-1325958191,3116345,3118337,-1289044064,97526364,101577,3178851,3357,1942563026,
			104431,3327612,-690213213,-336545092,-934396624,109413500,-902467812,-901910120,-892481938,
			-891974699,-889473228,-853085557,111433423,-15964427,3625364,1600705834,113101617,
			-1186450280,-1186449859,384941930,89810921,678404945,-352237448,-358647942,1680001968,
			131935256,2081021685
	 };
	//operator
	 public static final String operator[]={"+","-","*","/","%","=",">","<","!","==","!=",">=","<=","++","--","&","&&","||","[","]"};
	//separator
	 public static final String separator []={",",";","(",")","{","}","\'","\"",":","#"};
			
}
