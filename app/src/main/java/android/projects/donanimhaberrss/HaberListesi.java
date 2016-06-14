package android.projects.donanimhaberrss;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Created by omer on 9.5.2016.
 */
public class HaberListesi extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener,
        AdapterView.OnItemClickListener
{
    Elements haberListesi = new Elements();
    SwipeRefreshLayout swipeRefreshLayout;

    ListView lv;
    BaseAdapter ba;
    LayoutInflater layoutInflater;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_haber_listesi);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);

        swipeRefreshLayout.post(new Runnable()
        {
            public void run()
            {
                swipeRefreshLayout.setRefreshing(true);
                onRefresh();

            }
        });

        lv = (ListView) findViewById(R.id.haberListesi);

        lv.setOnItemClickListener(this);
        layoutInflater = LayoutInflater.from(this);

        ba = new BaseAdapter() {
            @Override
            public int getCount() {
                return haberListesi.size();
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            public View getView(int i, View v, ViewGroup parent)
            {
                if (v == null)
                    v = layoutInflater.inflate(R.layout.haber_item, null);

                // Başlık, Tarih ve Resim View'larına Eriş
                TextView tvBaslik = (TextView) v.findViewById(R.id.tvBaslik);
                TextView tvTarih = (TextView) v.findViewById(R.id.tvTarih);
                ImageView iv = (ImageView) v.findViewById(R.id.iv);

                Element haber = haberListesi.get(i);
                String resimAdr = haber.select("enclosure").first().attr("url");


                tvBaslik.setText(haber.select("title").text());
                tvTarih.setText(haber.select("pubDate").text());
                // Picasso Ile ASYNC Resim
                Picasso.with(HaberListesi.this)
                        .load(resimAdr)

                        .placeholder(R.drawable.bekle)
                        .error(android.R.drawable.ic_menu_delete)
                        .into(iv);



                return v;
            }
        };

        lv.setAdapter(ba);

    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        Data.secilenHaber = haberListesi.get(position);
       startActivity(new Intent(this, HaberGoster.class));
    }

    public void onRefresh()
    {
        new RSS_DOWNLOADER().execute();
    }

    class RSS_DOWNLOADER extends AsyncTask<String, String, String>
    {
        protected String doInBackground(String... params) {
            try
            {
                String adr = "http://www.donanimhaber.com/rss/tum/";
                haberListesi =
                        Jsoup.connect(adr).timeout(30000)
                        .userAgent("Mozilla")
                        .get().select("item");

                Log.e("x","Çekilen Haber Sayısı : "+haberListesi.size());
                return "OK";
            } catch (Exception e)
            {
                return e.toString();
            }
        }


        protected void onPostExecute(String s) {
            swipeRefreshLayout.setRefreshing(false);
            if (s.equals("OK"))
            {
                // ListView'i Yenile
                ba.notifyDataSetChanged();
            }
            else
            {
                AlertDialog.Builder adb = new AlertDialog.Builder(HaberListesi.this);
                adb.setTitle("Hata")
                        .setMessage(s)
                        .setPositiveButton("Tamam",null)
                        .show();
            }
        }
    }
}
