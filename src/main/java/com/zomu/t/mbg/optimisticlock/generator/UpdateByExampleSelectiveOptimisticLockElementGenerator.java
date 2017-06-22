package com.zomu.t.mbg.optimisticlock.generator;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.config.Context;

public class UpdateByExampleSelectiveOptimisticLockElementGenerator {

	/** バージョンカラム */
	private IntrospectedColumn versionColumn = null;

	/** テーブル情報 */
	protected IntrospectedTable introspectedTable = null;

	/** Generatorコンテキスト */
	protected Context context = null;

	/**
	 * コンストラクタ
	 * 
	 * @param introspectedTable
	 * @param context
	 */
	public UpdateByExampleSelectiveOptimisticLockElementGenerator(IntrospectedTable introspectedTable, Context context,
			IntrospectedColumn versionColumn) {
		this.introspectedTable = introspectedTable;
		this.context = context;
		this.versionColumn = versionColumn;
	}

	/**
	 * XML要素を作成して取得します。
	 * 
	 * @return 作成したXML要素
	 */
	public XmlElement getElements() {

		XmlElement answer = new XmlElement("update");

		answer.addAttribute(
				new Attribute("id", introspectedTable.getUpdateByExampleSelectiveStatementId() + "OptimisticLock"));

		answer.addAttribute(new Attribute("parameterType", "map"));

		context.getCommentGenerator().addComment(answer);

		StringBuilder sb = new StringBuilder();
		sb.append("update ");
		sb.append(introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime());
		answer.addElement(new TextElement(sb.toString()));

		XmlElement dynamicElement = new XmlElement("set");
		answer.addElement(dynamicElement);

		for (IntrospectedColumn introspectedColumn : introspectedTable.getAllColumns()) {
			XmlElement isNotNullElement = new XmlElement("if");
			sb.setLength(0);
			sb.append(introspectedColumn.getJavaProperty("record."));
			sb.append(" != null");
			isNotNullElement.addAttribute(new Attribute("test", sb.toString()));
			dynamicElement.addElement(isNotNullElement);

			sb.setLength(0);
			sb.append(MyBatis3FormattingUtilities.getAliasedEscapedColumnName(introspectedColumn));
			sb.append(" = ");
			sb.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn, "record."));
			sb.append(',');

			isNotNullElement.addElement(new TextElement(sb.toString()));
		}

		answer.addElement(getUpdateByExampleIncludeElement());

		// 楽観ロック用のversionカラムの条件を追加する
		StringBuilder optimisticLockSb = new StringBuilder();
		optimisticLockSb.append(" and ");
		optimisticLockSb.append(MyBatis3FormattingUtilities.getEscapedColumnName(versionColumn));
		optimisticLockSb.append(" = ");
		optimisticLockSb.append(MyBatis3FormattingUtilities.getParameterClause(versionColumn));

		answer.addElement(new TextElement(optimisticLockSb.toString()));

		return answer;
	}

	protected XmlElement getUpdateByExampleIncludeElement() {
		XmlElement ifElement = new XmlElement("if"); //$NON-NLS-1$
		ifElement.addAttribute(new Attribute("test", "_parameter != null")); //$NON-NLS-1$ //$NON-NLS-2$

		XmlElement includeElement = new XmlElement("include"); //$NON-NLS-1$
		includeElement.addAttribute(new Attribute("refid", //$NON-NLS-1$
				introspectedTable.getMyBatis3UpdateByExampleWhereClauseId()));
		ifElement.addElement(includeElement);

		return ifElement;
	}

}
