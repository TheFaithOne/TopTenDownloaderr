package learnprogramming.academy

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.net.URL
import kotlin.properties.Delegates

class FeedEntry {
    var title: String = ""
    var name: String = ""
    var artist: String = ""
    var releaseDate: String = ""
    var summary: String = ""
    var imageUrl: String = ""

    override fun toString(): String {
        return """
            title = $title
            name = $name
            artist = $artist
            release date = $releaseDate
            summary = $summary
            imageURL = $imageUrl
            """.trimIndent()
    }
}

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private var downloadData: DownloadData? = null
    private var feedUrl: String = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml"
    private var feedLimit = 10
    private var cachedUrl = "INVALIDATED"
    //have to use the nullable type because a menu item and consecutive items in menu could be null
    //Also cannot create more than one instance of AsyncTask!! Before closing one have to make sure it's not null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(savedInstanceState != null) {
            feedUrl = savedInstanceState.getString("CurrentURL", "")
            feedLimit = savedInstanceState.getInt("CurrentLimit")
        }
        downloadUrl(feedUrl.format(feedLimit))
    }

    private fun downloadUrl(feedUrl: String){
        if(feedUrl != cachedUrl) {
            Log.d(TAG, "downloadUrl called")
            downloadData = DownloadData(this, xmlListView)
            downloadData?.execute(feedUrl)
            cachedUrl = feedUrl
            Log.d(TAG, "downloadUrl finished..")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.feeds_menu, menu)

        if(feedLimit == 10){
            menu?.findItem(R.id.mnu10)?.isChecked = true
        } else{
            menu?.findItem(R.id.mnu25)?.isChecked = true
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId){
            R.id.mnuFree ->
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml"
            R.id.mnuPaid ->
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/toppaidapplications/limit=%d/xml"
            R.id.mnuSongs ->
                feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topsongs/limit=%d/xml"
            R.id.mnu10, R.id.mnu25 ->{
                if(!item.isChecked){
                    item.isChecked = true
                    feedLimit = 35 - feedLimit
                    Log.d(TAG, "onOptionsItemsSelected: ${item.title} setting feedLimit to $feedLimit")
                } else{
                    Log.d(TAG, "onOptionsItemsSelected: ${item.title} setting feedLimit unchanged")
                }
            }
            R.id.mnuRefresh ->{
                cachedUrl = "INVALIDATED"
            }
            else -> return super.onOptionsItemSelected(item)
        }
        downloadUrl(feedUrl.format(feedLimit))
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        downloadData?.cancel(true)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("CurrentURL", feedUrl)
        outState.putInt("CurrentLimit", feedLimit)
    }

    companion object {
        private class DownloadData(context: Context, listView: ListView) :
            AsyncTask<String, Void, String>() {
            private val TAG = "DownloadData"
            var propContext: Context by Delegates.notNull()
            var propListView: ListView by Delegates.notNull()

            init {
                propContext = context
                propListView = listView
            }

            override fun onPostExecute(result: String) {
                super.onPostExecute(result)
                val parseApplication = ParseApplication()
                parseApplication.parse(result)

                /*val arrayAdapter = ArrayAdapter<FeedEntry>(
                    propContext,
                    R.layout.list_item,
                    parseApplication.applications
                )
                propListView.adapter = arrayAdapter*/

                val feedAdapter = FeedAdapter(propContext, R.layout.list_record, parseApplication.applications)
                propListView.adapter = feedAdapter
            }

            override fun doInBackground(vararg url: String?): String {
                Log.d(TAG, "doInBackground starts with ${url[0]}")
                val rssFeed = downloadXML(url[0])
                if (rssFeed.isEmpty()) {
                    Log.e(TAG, "doInBackground: Error downloading..")
                }
                return rssFeed
            }

            private fun downloadXML(urlPath: String?): String {
                return URL(urlPath).readText()
            }

        }
    }
}


/*val xmlResult = StringBuilder()

              //Need for a try catch block because reading data from unknown source e.g. Internet or sever could be down
              try {
                  val url = URL(urlPath)
                  val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
                  val response = connection.responseCode
                  Log.d(TAG, "downloadXML response was: $response")

                  *//* val inputStream = connection.inputStream
                     val inputStreamReader = InputStreamReader(inputStream)
                     val reader = BufferedReader(inputStreamReader)*//*

                 *//*   val reader = BufferedReader(InputStreamReader(connection.inputStream))

                    val inputBuffer = CharArray(500)
                    var charsRead = 0
                    while (charsRead >= 0) {
                        charsRead = reader.read(inputBuffer)
                        // if buffered reader reaches the end of stream it .read function returns a value < 0
                        // so we loop until the value is >=0. Returning 0 doesn't necessary means we've reached the EOF
                        if (charsRead > 0) { //no point appending nothing.
                            xmlResult.append(String(inputBuffer, 0, charsRead))
                        }
                    }
                    reader.close()*//*

                    connection.inputStream.buffered().reader().use { xmlResult.append(it.readText()) }
                    Log.d(TAG, "Received ${xmlResult.length} bytes..")

                    return xmlResult.toString()
                } catch(e: Exception){
                    val errorMessage: String = when(e){
                        is MalformedURLException -> "downloadXML: Invalid URL ${e.message}"
                        is IOException -> "downloadXML: IO Exception reading data: ${e.message}"
                        is SecurityException -> { e.printStackTrace()
                            "downloadXML: Security exception. Need permissions? ${e.message}"}
                        else -> "Unknown error: ${e.message}"
                    }
                }
                return "" //If there's been a problem. Return an empty string
            }
        }
    }*/
