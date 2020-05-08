# ConfuseIOSByJava
用JAVA实现的IOS工程代码混淆

##### 0、混淆的基本逻辑
	
	1、获取需要混淆的类的名称，生成对应的混淆后的名称（只识别 h 文件，顺带处理同名的 h, m, xib 三种文件）
	2、获取需要混淆的方法名称，生成对应的混淆后的方法名称（只识别带有特定标识的方法名，需要手动标记）
	3、根据语法遍历工程里的所有文件进行混淆改动，仅会被改动到的文件后缀 xib, h, m, pch, pbxproj

##### 1、本代码能正常混淆的前置条件，需要调整的自行改动

1.1 混淆类名来自所有 h 文件（会替换同名的 h , m , xib 三种文件）（可以指定不混淆目录）比如：
	
	Test.h  Test.m  Test.xib
	...


1.2 给需要混淆的方法名前一行加特定标识，这里只识别 m 文件（标识可以自定定义调整）比如：
	
	// CONFUSED_METHOD_TAG
	-(void)reloadJPushInfo {
	...


1.3 自行安装 IDEA 或运行此代码，比如：
	
	这里没有比如，自己搞定吧


##### 2、代码中的变量定义

	PROJECT_PATH                  工程的本地路径
	CONFUSED_METHOD_TAG           混淆的方法的标识
	IGNORE_CLASS_PATH_ARRAY       此目录下的文件名不做混淆
	IGNORE_METHOD_PATH_ARRAY      此目录下的代码里的方法名不做混淆调整


##### 3、代码中的部分固定字符串含义

	NCLN                          混淆后的新类名前缀，如：NCLN1234
	nmtm                          混淆后的新方法名前缀，如：nmtm1234
