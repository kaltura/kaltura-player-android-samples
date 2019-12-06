package com.kaltura.kalturaplayertestapp

import android.content.Context
import androidx.annotation.WorkerThread
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.*
import com.kaltura.kalturaplayertestapp.models.Configuration
import java.util.*
import java.util.concurrent.*

object ConfigurationUtil {

    private val TAG = "ConfigurationUtil"

    private val EXECUTOR = ThreadPoolExecutor(2, 4, 60,
            TimeUnit.SECONDS, LinkedBlockingQueue())

    private val RESTAURANT_URL_FMT = "https://storage.googleapis.com/firestorequickstarts.appspot.com/food_%d.png"
    private val MAX_IMAGE_NUM = 22

    private val NAME_FIRST_WORDS = arrayOf("Foo", "Bar", "Baz", "Qux", "Fire", "Sam's", "World Famous", "Google", "The Best")

    private val NAME_SECOND_WORDS = arrayOf("Restaurant", "Cafe", "Spot", "Eatin' Place", "Eatery", "Drive Thru", "Diner")


    /**
     * Create a random Restaurant POJO.
     */
    fun getRandom(context: Context): Configuration {
        val configuration = Configuration()
        val random = Random()

        //configuration.setId(id);//(String.valueOf(random.nextInt(5000000)));
        configuration.title = getRandomName(random)
        configuration.json = "{\"widget\": {\n" +
                "    \"debug\": \"on\",\n" +
                "    \"window\": {\n" +
                "        \"title\": \"Sample Konfabulator Widget\",\n" +
                "        \"name\": \"main_window\",\n" +
                "        \"width\": 500,\n" +
                "        \"height\": 500\n" +
                "    },\n" +
                "    \"image\": { \n" +
                "        \"src\": \"Images/Sun.png\",\n" +
                "        \"name\": \"sun1\",\n" +
                "        \"hOffset\": 250,\n" +
                "        \"vOffset\": 250,\n" +
                "        \"alignment\": \"center\"\n" +
                "    },\n" +
                "    \"text\": {\n" +
                "        \"data\": \"Click Here\",\n" +
                "        \"size\": 36,\n" +
                "        \"style\": \"bold\",\n" +
                "        \"name\": \"text1\",\n" +
                "        \"hOffset\": 250,\n" +
                "        \"vOffset\": 100,\n" +
                "        \"alignment\": \"center\",\n" +
                "        \"onMouseUp\": \"sun1.opacity = (sun1.opacity / 100) * 90;\"\n" +
                "    }\n" +
                "}} "
        configuration.type = 0

        // Note: average rating intentionally not set

        return configuration
    }


    /**
     * Get a random image.
     */
    private fun getRandomImageUrl(random: Random): String {
        // Integer between 1 and MAX_IMAGE_NUM (inclusive)
        val id = random.nextInt(MAX_IMAGE_NUM) + 1

        return String.format(Locale.getDefault(), RESTAURANT_URL_FMT, id)
    }


    /**
     * Get price represented as dollar signs.
     */
    fun getPriceString(priceInt: Int): String {
        when (priceInt) {
            1 -> return "$"
            2 -> return "$$"
            3 -> return "$$$"
            else -> return "$$$"
        }
    }

    /**
     * Delete all documents in a collection. Uses an Executor to perform work on a background
     * thread. This does *not* automatically discover and delete subcollections.
     */
    private fun deleteCollection(collection: CollectionReference,
                                 batchSize: Int,
                                 executor: Executor): Task<Void> {

        // Perform the delete operation on the provided Executor, which allows us to use
        // simpler synchronous logic without blocking the main thread.
        return Tasks.call(executor, Callable<Void> {
            // Get the first batch of documents in the collection
            var query = collection.orderBy("__name__").limit(batchSize.toLong())

            // Get a list of deleted documents
            var deleted = deleteQueryBatch(query)

            // While the deleted documents in the last batch indicate that there
            // may still be more documents in the collection, page down to the
            // next batch and delete again
            while (deleted.size >= batchSize) {
                // Move the query cursor to start after the last doc in the batch
                val last = deleted[deleted.size - 1]
                query = collection.orderBy("__name__")
                        .startAfter(last.id)
                        .limit(batchSize.toLong())

                deleted = deleteQueryBatch(query)
            }

            null
        })

    }

    /**
     * Delete all results from a query in a single WriteBatch. Must be run on a worker thread
     * to avoid blocking/crashing the main thread.
     */
    @WorkerThread
    @Throws(Exception::class)
    private fun deleteQueryBatch(query: Query): List<DocumentSnapshot> {
        val querySnapshot = Tasks.await(query.get())

        val batch = query.firestore.batch()
        for (snapshot in querySnapshot) {
            batch.delete(snapshot.reference)
        }
        Tasks.await(batch.commit())

        return querySnapshot.documents
    }

    /**
     * Delete all restaurants.
     */
    fun deleteAll(): Task<Void> {
        val ref = FirebaseFirestore.getInstance().collection("restaurants")
        return deleteCollection(ref, 25, EXECUTOR)
    }

    private fun getRandomRating(random: Random): Double {
        val min = 1.0
        return min + random.nextDouble() * 4.0
    }

    private fun getRandomName(random: Random): String {
        return (getRandomString(NAME_FIRST_WORDS, random) + " "
                + getRandomString(NAME_SECOND_WORDS, random))
    }

    private fun getRandomString(array: Array<String>, random: Random): String {
        val ind = random.nextInt(array.size)
        return array[ind]
    }

    private fun getRandomInt(array: IntArray, random: Random): Int {
        val ind = random.nextInt(array.size)
        return array[ind]
    }

}
