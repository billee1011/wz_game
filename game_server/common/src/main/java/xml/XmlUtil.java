package xml;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.*;
import java.lang.reflect.Field;

/**
 * Created by Administrator on 2016/12/7.
 */
public class XmlUtil {

	public static String ObjectToXml(Object obj) {
		Element root = new Element("xml");
		Document doc = new Document(root);
		Field[] allFields = obj.getClass().getDeclaredFields();
		int length = allFields.length;
		for (int i = 0; i < length; i++) {
			Field filed = allFields[i];
			filed.setAccessible(true);
			try {
				root.addContent(new Element(filed.getName()).setText(filed.get(obj).toString()));
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		XMLOutputter XMLOut = new XMLOutputter(Format.getCompactFormat().setEncoding("utf-8").setIndent(" "));
		ByteBuf buffer = Unpooled.buffer();
		BufferedOutputStream out = new BufferedOutputStream(new ByteBufOutputStream(buffer));
		try {
			XMLOut.output(doc, out);
			return new String(buffer.array(), "utf-8");
		} catch (IOException e) {
			return null;
		}
	}

	public static String getWxPreId(String xmlStr) {
		SAXBuilder builder = new SAXBuilder();
		try {
			Reader reader = new InputStreamReader(new ByteArrayInputStream(xmlStr.getBytes("utf-8")));
			Document doc = builder.build(reader);
			Element wxPrdId = doc.getRootElement().getChild("prepay_id");
			String content = wxPrdId.getValue();
			return content;
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
}
