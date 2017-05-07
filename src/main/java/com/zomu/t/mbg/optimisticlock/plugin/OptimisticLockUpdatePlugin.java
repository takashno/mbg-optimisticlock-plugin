package com.zomu.t.mbg.optimisticlock.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.generator.api.FullyQualifiedTable;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.XmlElement;

import com.zomu.t.mbg.optimisticlock.generator.UpdateByPrimaryKeySelectiveOptimisticLockElementGenerator;

/**
 * 楽観ロックによる更新SQL作成プラグイン.
 * 
 * @author takashno
 *
 */
public class OptimisticLockUpdatePlugin extends PluginAdapter {

	/** バージョンカラム名 */
	private String versionColumnName = "version";

	/** バージョンカラム */
	private IntrospectedColumn versionColumn = null;

	/** 追加対象XML要素 */
	private Map<FullyQualifiedTable, List<XmlElement>> elementsToAdd;

	/**
	 * コンストラクタ
	 */
	public OptimisticLockUpdatePlugin() {
		super();
		elementsToAdd = new HashMap<FullyQualifiedTable, List<XmlElement>>();
	}

	/**
	 * 事前チェック処理
	 */
	@Override
	public boolean validate(List arg) {
		if (properties.getProperty("versionColumnName") != null) {
			versionColumnName = properties.getProperty("versionColumnName");
		}
		return true;
	}

	/**
	 * 初期化処理
	 */
	@Override
	public void initialized(IntrospectedTable introspectedTable) {
		super.initialized(introspectedTable);

		// version指定カラムが存在するかをチェックする
		boolean versionColumnExists = false;
		for (IntrospectedColumn ic : introspectedTable.getAllColumns()) {
			if (ic.getActualColumnName().toUpperCase().equals(versionColumnName.toUpperCase())) {
				versionColumnExists = true;
				versionColumn = ic;
				break;
			}
		}
		if (!versionColumnExists) {
			throw new RuntimeException(
					"versionを示すカラムが存在しません。デフォルト以外のカラムを使用したい場合には、プラグインの「versionColumnName」プロパティにカラムを指定してもらう必要があります。versionカラム："
							+ versionColumnName);
		}

	}

	/**
	 * {@inheritDoc}
	 * <hr>
	 * SQLマップに対して追加XML要素を追加します。
	 */
	@Override
	public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
		List<XmlElement> elements = elementsToAdd.get(introspectedTable.getFullyQualifiedTable());
		if (elements != null) {
			for (XmlElement element : elements) {
				document.getRootElement().addElement(element);
			}
		}
		return true;
	}

	/* ---------------------------------------------------------------------- */
	/* UpdateByPrimaryKeySelective */
	/* ---------------------------------------------------------------------- */
	/**
	 * {@inheritDoc}
	 * <hr>
	 * [updateByPrimaryKeySelective拡張用]<br>
	 * 楽観ロックによるUPDATEのSQLを作成します。
	 */
	@Override
	public boolean sqlMapUpdateByPrimaryKeySelectiveElementGenerated(XmlElement element,
			IntrospectedTable introspectedTable) {
		// プライマリーキー選択アップデートの楽観ロック処理のSQLMap生成処理
		UpdateByPrimaryKeySelectiveOptimisticLockElementGenerator xmlGen = new UpdateByPrimaryKeySelectiveOptimisticLockElementGenerator(
				introspectedTable, context, versionColumn);
		XmlElement newElement = xmlGen.getElements();
		addElement(newElement, introspectedTable.getFullyQualifiedTable());
		return true;
	}

	/**
	 * {@inheritDoc}
	 * <hr>
	 * [updateByPrimaryKeySelective拡張用]<br>
	 * 値設定フラグによるSET句に含めるかの判定を行うSQLを実行するMapperインタフェースのメソッドを作成します。
	 */
	@Override
	public boolean clientUpdateByPrimaryKeySelectiveMethodGenerated(Method method, Interface interfaze,
			IntrospectedTable introspectedTable) {
		addOptimisticLockMethod(method, interfaze, "", "");
		return true;
	}

	/* ---------------------------------------------------------------------- */

	/**
	 * 追加したXML要素を保存します。
	 * 
	 * @param newElement
	 * @param fqt
	 */
	private void addElement(XmlElement newElement, FullyQualifiedTable fqt) {
		List<XmlElement> elements = elementsToAdd.get(fqt);
		if (elements == null) {
			elements = new ArrayList<XmlElement>();
			elementsToAdd.put(fqt, elements);
		}
		elements.add(newElement);
	}

	/**
	 * 楽観ロック用メソッドを追加します。 <br>
	 * メソッド名は一律「OptimisticLock」が接尾辞として付与されます。
	 * 
	 * @param method
	 * @param interfaze
	 */
	private void addOptimisticLockMethod(Method method, Interface interfaze, String search, String replace) {

		// WithSettingメソッドを生成
		Method newMethod = new Method(method);

		for (Parameter p : newMethod.getParameters()) {
			if (p.getAnnotations().isEmpty()) {
				p.addAnnotation("@Param(\"" + p.getName() + "\")");
			}
		}

		// 既存メソッドにOptimisticLockを加えたメソッド名とする
		newMethod.setName(method.getName() + "OptimisticLock");

		// パタメータを追加
		Parameter settingParameter = new Parameter(versionColumn.getFullyQualifiedJavaType(),
				versionColumn.getJavaProperty());
		settingParameter.addAnnotation("@Param(\"" + versionColumn.getJavaProperty() + "\")");
		newMethod.addParameter(settingParameter);

		// パラメータクラスをインポートするように指定
		interfaze.addImportedType(versionColumn.getFullyQualifiedJavaType());
		interfaze.addMethod(newMethod);
	}

}
