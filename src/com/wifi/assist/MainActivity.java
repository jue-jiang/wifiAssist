package com.wifi.assist;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.wifi.assist.R;


public class MainActivity extends ActionBarActivity implements OnQueryTextListener  {
    String TAG=MainActivity.class.getName();
    String sortedResult="";
    ScrollView scrollView;
    TextView textView;
    SearchView searchView;
	ArrayList<Integer> matchedLine=new ArrayList<Integer>();
	Button forwardButton,backwardButton;
	int currentLineIndex;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        this.setTitle("WiFi√‹¬Î≤Èø¥÷˙ ÷");
//        Log.v(TAG, commandForResult("cat /data/misc/wifi/wpa_supplicant.conf"));
        String commandResult=commandForResult("cat /data/misc/wifi/wpa_supplicant.conf");
        sortedResult=sortByPriority(commandResult);
        textView=((TextView)findViewById(R.id.ssidTextView));
        textView.setText(sortedResult);

        scrollView=(ScrollView)findViewById(R.id.scrollView);
        forwardButton=(Button)findViewById(R.id.forwardButton);
        backwardButton=(Button)findViewById(R.id.backwardButton);
        forwardButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
				if (currentLineIndex==matchedLine.size()-1) {
					Toast.makeText(getApplicationContext(), "reach last match,go from first!", Toast.LENGTH_SHORT).show();
					currentLineIndex=0;
					scrollView.post(new Runnable() {
					    @Override
					    public void run() {
					        int y = textView.getLayout().getLineTop(matchedLine.get(currentLineIndex));
					        scrollView.scrollTo(0, y);
					    }
					});
				}else {
					currentLineIndex=currentLineIndex+1;
					scrollView.post(new Runnable() {
					    @Override
					    public void run() {
					        int y = textView.getLayout().getLineTop(matchedLine.get(currentLineIndex));
					        scrollView.scrollTo(0, y);
					    }
					});					
				}
			}
		});//forward button
        backwardButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (currentLineIndex==0) {
					Toast.makeText(getApplicationContext(), "reach first match,go from last!", Toast.LENGTH_SHORT).show();
					currentLineIndex=matchedLine.size()-1;
					scrollView.post(new Runnable() {
					    @Override
					    public void run() {
					        int y = textView.getLayout().getLineTop(matchedLine.get(currentLineIndex));
					        scrollView.scrollTo(0, y);
					    }
					});
				}else {
					currentLineIndex=currentLineIndex-1;
					scrollView.post(new Runnable() {
					    @Override
					    public void run() {
					        int y = textView.getLayout().getLineTop(matchedLine.get(currentLineIndex));
					        scrollView.scrollTo(0, y);
					    }
					});					
				}		
			}
		});//backward button 
        backwardButton.setVisibility(View.INVISIBLE);
        forwardButton.setVisibility(View.INVISIBLE);
        textView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
		        backwardButton.setVisibility(View.INVISIBLE);
		        forwardButton.setVisibility(View.INVISIBLE);				
			}
		});
    }
    
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        
        searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.menu_search)); 
        searchView.setOnQueryTextListener(this);
        return true; 
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }
    @Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
	public String commandForResult(String command) {
		
		try {
		    Process process = Runtime.getRuntime().exec("su");
		    DataOutputStream outputStream = null;
		    outputStream = new DataOutputStream(process.getOutputStream());
            outputStream.writeBytes(command+"\n");
            outputStream.flush();
            outputStream.writeBytes("exit\n");
            outputStream.flush();
            
		    BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
			StringBuilder total = new StringBuilder();
			String line;
			while ((line = in.readLine()) != null) {
			    total.append(line);
			    total.append("\n");
			}
			return total.toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "error";
		}
	}
	class NetworkPara{
		String paraString;
		int priority;
	}
	String sortByPriority(String input){
		String [] stringPerLine=input.split("\n");
		ArrayList<NetworkPara> list=new ArrayList<MainActivity.NetworkPara>();
		int start=0,end=0;
		NetworkPara networkPara = null;
		for (int i = 0; i < stringPerLine.length; i++) {
			if (stringPerLine[i].contains("network={")) {
				start=1;
				end=0;
				networkPara=new NetworkPara();
				networkPara.paraString="";
			}
			if (start==1) {
				if (networkPara!=null) {
					networkPara.paraString=networkPara.paraString.concat(stringPerLine[i])+"\n";	
				}
				if (stringPerLine[i].contains("priority")) {
					String []prioSplit=stringPerLine[i].split("=");
					networkPara.priority=Integer.parseInt(prioSplit[prioSplit.length-1]);
				}
				if (stringPerLine[i].contains("}")) {
					start=0;
					end=1;
				}
			}
			if (end==1) {
				list.add(networkPara);
			}
		}
		 Collections.sort(list, new Comparator() {
             public int compare(Object o1, Object o2) {
                     return ((Comparable) ((NetworkPara) (o2)).priority)
                             .compareTo(((NetworkPara) (o1)).priority);
                     }
             });
        
		String result="";
		for (int i = 0; i < list.size(); i++) {
			result=result.concat(list.get(i).paraString);
		}
		return result;
	}

	@Override
	public boolean onQueryTextChange(String arg0) {
		// TODO Auto-generated method stub
		Log.v(TAG, "querychange"+arg0);
		return false;
	}

	@Override
	public boolean onQueryTextSubmit(String arg0) {
		// TODO Auto-generated method stub
		Log.v(TAG, "querysubmit"+arg0);
		matchedLine.clear();
		String []stringSplit=sortedResult.split("\n");
		for (int i = 0; i < stringSplit.length; i++) {
			//case insensitive match
			if (stringSplit[i].toLowerCase().contains(arg0.toLowerCase())) {
				matchedLine.add(i);
			}
		}
//		Intent searchIntent=new Intent(MainActivity.this,MainActivity.class);
//		searchIntent.putIntegerArrayListExtra("matched", matchedLine);
//		startActivity(searchIntent);
//		MainActivity.this.finish();
		if (matchedLine.size()==0) {
			Toast.makeText(getApplicationContext(), "no match!", Toast.LENGTH_SHORT).show();
			return false;
		}else if (matchedLine.size()==1) {
			
		}
		else {
			forwardButton.setVisibility(View.VISIBLE);
			backwardButton.setVisibility(View.VISIBLE);
		}
		scrollView.post(new Runnable() {
		    @Override
		    public void run() {
		        int y = textView.getLayout().getLineTop(matchedLine.get(0));
		        scrollView.scrollTo(0, y);
		    }
		});
		searchView.clearFocus();
		return false;
	}

}
