package in.dobro.rubible;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopFieldCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Searchaction extends Activity {
	
	public static File INDEX_DIR;
	
	Button addbutton;
	TextView tvsearch;
	EditText searchtext;
	String searchvalue;
	
	  @Override
	  protected void onCreate(Bundle savedInstanceState) {
		
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.searching);
	   
	    try {
			Lucenesearch();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	  }

	private void Lucenesearch() throws IOException, ParseException {
		
		Intent searchintent = getIntent(); 
		searchvalue = searchintent.getStringExtra("extrasearchvalue");
		
		setTitle("Поиск слова: \"" + searchvalue + "\"");
		
		tvsearch = (TextView)findViewById(R.id.textViewsearch);
		
		File root = android.os.Environment.getExternalStorageDirectory();
		File dir = new File(root.getAbsolutePath() + "/rubible_search");
		
		//полнотекстовый поиск	
	    INDEX_DIR = new File(dir.toString());
		
		Directory index = FSDirectory.open(INDEX_DIR); 
		StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_35);
		
		String querytext = searchvalue;

    	Query q = new QueryParser(Version.LUCENE_35, "poemtext", analyzer).parse(querytext);
    	
    	int hitsPerPage = 500;
    	
    	IndexReader reader = IndexReader.open(index);
    	
    	IndexSearcher searcher = new IndexSearcher(reader);
   
    	Sort sort = new Sort(); 
    	sort.setSort(new SortField("bible", SortField.INT));
    	
    	TopFieldCollector collector = TopFieldCollector.create(sort, hitsPerPage, false, true, true, false);
    	
    	searcher.search(q, collector);
    	//searcher.search(q, 50, sort);
    	
    	ScoreDoc[] hits = collector.topDocs().scoreDocs;
    	
	    String result = "<b>Результаты поиска: " + hits.length + " совпадений</b><br><br>";

	    tvsearch.append(Html.fromHtml(result));
	    
	    for(int i=0;i<hits.length;++i) {
    	    int docId = hits[i].doc;
    	    Document d = searcher.doc(docId);
    	    
    	    final String getbible = d.get("bible");
    	    
    	    String namebible = d.get("indexbiblename");
    	    
    	    final String getchapter = d.get("chapter");
    	    String getpoem = d.get("poem");
    	    String getpoemtext = d.get("poemtext");

    	    result = "<font color='gray'>Книга " + getbible + ", глава " + getchapter + ", стих " + getpoem + "</font><br>";
    	    //result += "<a style='text-decoration:none;color:black;' href=http://null_" + getbible + "_" + getchapter + "_" + getpoem + ">" + getpoemtext + "</a><br><br>";

    	    tvsearch.append(Html.fromHtml(result));
    	    
    	    SpannableString link = makeLinkSpan(getpoemtext, new View.OnClickListener() {          
    	        @Override
    	        public void onClick(View v) {
    	        	//tv.setText("click");
    	        	String textlink = "Книга " + getbible + ", глава " + getchapter;
    	        	Toast.makeText(getApplicationContext(), textlink, Toast.LENGTH_SHORT).show();
    	        }
    	    });
    	    
    	    tvsearch.append(link);
    	 
    	    result = "<br><br>";
    	    
    	    tvsearch.append(Html.fromHtml(result));  
    	    
    	}

	    makeLinksFocusable(tvsearch);	
		
	}

	private void makeLinksFocusable(TextView tv) {
	    MovementMethod m = tv.getMovementMethod();  
	    if ((m == null) || !(m instanceof LinkMovementMethod)) {  
	        if (tv.getLinksClickable()) {  
	            tv.setMovementMethod(LinkMovementMethod.getInstance());  
	        }  
	    }  
	}

	private SpannableString makeLinkSpan(CharSequence text, View.OnClickListener listener) {
	    SpannableString link = new SpannableString(text);
	    link.setSpan(new ClickableString(listener), 0, text.length(), 
	        SpannableString.SPAN_INCLUSIVE_EXCLUSIVE);
	    return link;
	}
	
	private static class ClickableString extends ClickableSpan {  
	    private View.OnClickListener mListener;          
	    public ClickableString(View.OnClickListener listener) {              
	        mListener = listener;  
	    }          
	    @Override  
	    public void onClick(View v) {  
	        mListener.onClick(v);  
	    }        
	}

}
