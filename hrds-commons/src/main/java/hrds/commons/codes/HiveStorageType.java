package hrds.commons.codes;

import hrds.commons.exception.AppSystemException;
/**Created by automatic  */
/**代码类型名：hive文件存储类型  */
public enum HiveStorageType {
	/**TEXTFILE<TEXTFILE>  */
	TEXTFILE("1","TEXTFILE","51","hive文件存储类型"),
	/**SEQUENCEFILE<SEQUENCEFILE>  */
	SEQUENCEFILE("2","SEQUENCEFILE","51","hive文件存储类型"),
	/**PARQUET<PARQUET>  */
	PARQUET("3","PARQUET","51","hive文件存储类型"),
	/**CSV<CSV>  */
	CSV("4","CSV","51","hive文件存储类型"),
	/**ORC<ORC>  */
	ORC("5","ORC","51","hive文件存储类型");

	private final String code;
	private final String value;
	private final String catCode;
	private final String catValue;

	HiveStorageType(String code,String value,String catCode,String catValue){
		this.code = code;
		this.value = value;
		this.catCode = catCode;
		this.catValue = catValue;
	}
	public String getCode(){return code;}
	public String getValue(){return value;}
	public String getCatCode(){return catCode;}
	public String getCatValue(){return catValue;}

	/**根据指定的代码值转换成中文名字
	* @param code   本代码的代码值
	* @return
	*/
	public static String ofValueByCode(String code) {
		for (HiveStorageType typeCode : HiveStorageType.values()) {
			if (typeCode.getCode().equals(code)) {
				return typeCode.value;
			}
		}
		throw new AppSystemException("根据"+code+"没有找到对应的代码项");
	}

	/**根据指定的代码值转换成对象
	* @param code   本代码的代码值
	* @return
	*/
	public static HiveStorageType ofEnumByCode(String code) {
		for (HiveStorageType typeCode : HiveStorageType.values()) {
			if (typeCode.getCode().equals(code)) {
				return typeCode;
			}
		}
		throw new AppSystemException("根据"+code+"没有找到对应的代码项");
	}

	/**
	* 获取代码项的中文类名名称
	* @return
	*/
	public static String ofCatValue(){
		return HiveStorageType.values()[0].getCatValue();
	}

	/**
	* 获取代码项的分类代码
	* @return
	*/
	public static String ofCatCode(){
		return HiveStorageType.values()[0].getCatCode();
	}

	/**
	* 禁止使用类的tostring()方法
	* @return
	*/
	@Override
	public String toString() {
		throw new AppSystemException("There's no need for you to !");
	}
}
