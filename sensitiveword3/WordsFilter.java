/**
 */
package sensitiveword3;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author zyong
 * 
 */
public class WordsFilter {

	Node tree = new Node();

	public int initFilter() {
		int sum = 0;
		try (InputStream is = WordsFilter.class.getResourceAsStream("words.dict");
				InputStreamReader reader = new InputStreamReader(is, "UTF-8")) {
			Properties prop = new Properties();
			prop.load(reader);
			Set<String> set = prop.stringPropertyNames();
			for (String s : set) {
				insertWord(s);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return sum;
	}

	public void insertWord(String w) {
		Node tree = this.tree;
		Node son = null;
		char[] charArray = w.toCharArray();

		int length = charArray.length;
		for (int i = 0; i < length; i++) {
			String c = String.valueOf(charArray[i]);
			// son = tree.next(c);
			if (tree.next(c) == null) {
				if (i < length - 1) {
					tree = tree.insert(c);
				} else {
					tree = tree.insertLast(c);
				}
			} else {
				tree = tree.next(c);
			}
		}
		tree.setEnd(true);
	}

	public void deleteWord(String w) {
		deleteWord(w.toCharArray(), null, this.tree, 0, new Over());
	}

	public void deleteWord(char[] cs, Node pre, Node owner, int i, Over over) {
		Node temp = null;
		String c1 = null;
		String c2 = null;
		String c = null;

		c = String.valueOf(cs[i]);

		if ((temp = owner.next(c)) != null && i < cs.length - 1) {
			c1 = c;
			c2 = String.valueOf(cs[++i]);
			pre = owner;
			owner = temp;
			deleteWord(cs, pre, owner, i, over);
		} else {
			return;
		}
		if (!over.isOver()) {
			owner.remove(c2);
			over.setOver(true);
		}
	}

	public String filterSensitive(String txt, char rep) {
		int length = txt.length();

		char[] charArray = txt.toCharArray();
		List<Integer> buffer = new ArrayList<Integer>();
		Node son = this.tree;
		Node temp;
		StringBuilder sb = new StringBuilder(txt);
		long currentTimeMillis = System.currentTimeMillis();
		for (int i = 0; i < length; i++) {
			String s = String.valueOf(charArray[i]);
			if (!isSymbol(s)) {
				if ((temp = son.next(s)) != null) {
					son = temp;
					buffer.add(i);
					if (son.isEnd()) {
						buffer.add(-1);
					}
				} else if (!buffer.isEmpty()) {
					if (buffer.contains(-1)) {
						filter(sb, buffer, rep);
					}
					buffer.clear();
					son = this.tree;
				}
			}
		}
		System.out.println(System.currentTimeMillis() - currentTimeMillis);
		if (!buffer.isEmpty() && son.isEnd()) {
			filter(sb, buffer, rep);
		}
		return sb.toString();
	}

	public String filterSensitiveAndHtml(String txt, char rep) {
		int length = txt.length();

		char[] charArray = txt.toCharArray();
		LinkedList<Integer> buffer = new LinkedList<Integer>();
		Node son = this.tree;
		Node temp;
		txt = txt.replaceAll("</?[^<]+>", "");
		StringBuilder sb = new StringBuilder(txt);
		long currentTimeMillis = System.currentTimeMillis();
		for (int i = 0; i < length; i++) {
			String s = String.valueOf(charArray[i]);
			if (!isSymbol(s)) {
				if ((temp = son.next(s)) != null) {
					son = temp;
					buffer.add(i);
					if (son.isEnd()) {
						buffer.add(-1);
					}
				} else if (!buffer.isEmpty()) {
					if (son.isEnd()) {
						filter(sb, buffer, rep);
					}
					buffer.clear();
					son = this.tree;
					if ((temp = son.next(s)) != null) {
						son = temp;
						buffer.add(i);
					}
				}
			}
		}
		System.out.println(System.currentTimeMillis() - currentTimeMillis);
		if (!buffer.isEmpty() && son.isEnd()) {
			filter(sb, buffer, rep);
		}
		return sb.toString();
	}

	private void filter(StringBuilder sb_, List<Integer> buffer, char rep) {
		StringBuilder sb = sb_;
		Integer[] arr = buffer.toArray(new Integer[buffer.size()]);
		int i;
		for (i = arr.length - 1; i >= 0; i--) {
			if (arr[i] == -1) {
				break;
			}
		}
		for (; i >= 0; i--) {
			if (arr[i] == -1) {
				continue;
			}
			sb.setCharAt(arr[i], rep);
		}
	}

	private boolean isSymbol(String c) {
		Pattern p = Pattern.compile("[\\pP\\pZ\\pS\\pM\\pC]", 2);
		return p.matcher(c).find();
	}
}

class Node {

	private Map<String, Node> data;
	private boolean end;

	Node() {
		data = new HashMap<String, Node>();
		end = false;
	}

	void remove(String s) {
		data.remove(s);
	}

	int size() {
		return data.size();
	}

	Node(int size) {
		if (size == 0) {
			end = true;
		} else {
			end = false;
		}
		data = new HashMap<String, Node>(size);
	}

	boolean isEnd() {
		return end;
	}

	void setEnd(boolean end) {
		this.end = end;
	}

	Node next(String w) {
		return data.get(w);
	}

	Node insert(String w) {
		Node node = new Node(2);
		data.put(w, node);
		return node;
	}

	public Node insertLast(String w) {
		Node node = new Node(0);
		data.put(w, node);
		return node;
	}

	boolean contains(String w) {
		if (data.containsKey(w)) {
			return true;
		}
		return false;
	}

}

class Over {
	boolean over = false;

	public boolean isOver() {
		return over;
	}

	public void setOver(boolean over) {
		this.over = over;
	}

}