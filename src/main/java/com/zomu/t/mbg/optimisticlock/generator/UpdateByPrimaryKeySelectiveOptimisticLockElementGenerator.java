package com.zomu.t.mbg.optimisticlock.generator;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;
import org.mybatis.generator.config.Context;

/**
 * <h2>プライマリキーによる選択アップデートの楽観ロック処理の更新SQLMap作成処理</h2>
 * <hr>
 * 楽観ロックに使用するカラムはコンストラクタで指定する必要があります。
 * 
 * @author takashno
 *
 */
public class UpdateByPrimaryKeySelectiveOptimisticLockElementGenerator {

	/** バージョンカラム */
	private IntrospectedColumn versionColumn = null;

	/** テーブル情報 */
	protected IntrospectedTable introspectedTable = null;

	/** Generatorコンテキスト */
	protected Context context = null;

	public UpdateByPrimaryKeySelectiveOptimisticLockElementGenerator(IntrospectedTable introspectedTable,
			Context context, IntrospectedColumn versionColumn) {
		this.introspectedTable = introspectedTable;
		this.context = context;
		this.versionColumn = versionColumn;
	}

	public XmlElement getElements() {
		XmlElement answer = new XmlElement("update");

		answer.addAttribute(
				new Attribute("id", introspectedTable.getUpdateByPrimaryKeySelectiveStatementId() + "OptimisticLock"));

		String parameterType;

		if (introspectedTable.getRules().generateRecordWithBLOBsClass()) {
			parameterType = introspectedTable.getRecordWithBLOBsType();
		} else {
			parameterType = introspectedTable.getBaseRecordType();
		}

		answer.addAttribute(new Attribute("parameterType", parameterType));

		context.getCommentGenerator().addComment(answer);

		StringBuilder sb = new StringBuilder();

		sb.append("update ");
		sb.append(introspectedTable.getFullyQualifiedTableNameAtRuntime());
		answer.addElement(new TextElement(sb.toString()));

		XmlElement dynamicElement = new XmlElement("set");
		answer.addElement(dynamicElement);

		for (IntrospectedColumn introspectedColumn : introspectedTable.getNonPrimaryKeyColumns()) {
			XmlElement isNotNullElement = new XmlElement("if");
			sb.setLength(0);
			sb.append(introspectedColumn.getJavaProperty("record."));
			sb.append(" != null");
			isNotNullElement.addAttribute(new Attribute("test", sb.toString()));
			dynamicElement.addElement(isNotNullElement);

			sb.setLength(0);
			sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
			sb.append(" = ");
			sb.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn, "record."));
			sb.append(',');

			isNotNullElement.addElement(new TextElement(sb.toString()));
		}

		boolean and = false;
		for (IntrospectedColumn introspectedColumn : introspectedTable.getPrimaryKeyColumns()) {
			sb.setLength(0);
			if (and) {
				sb.append("  and ");
			} else {
				sb.append("where ");
				and = true;
			}

			sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(introspectedColumn));
			sb.append(" = ");
			sb.append(MyBatis3FormattingUtilities.getParameterClause(introspectedColumn, "record."));
		}

		// 楽観ロック用のversionカラムの条件を追加する
		sb.append(" and ");
		sb.append(MyBatis3FormattingUtilities.getEscapedColumnName(versionColumn));
		sb.append(" = ");
		sb.append(MyBatis3FormattingUtilities.getParameterClause(versionColumn));

		answer.addElement(new TextElement(sb.toString()));

		return answer;
	}
}
