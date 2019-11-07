package com.kaltura.kalturaplayertestapp.adapter

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.*
import java.util.ArrayList


/**
 * RecyclerView adapter for displaying the results of a Firestore [Query].
 *
 * Note that this class forgoes some efficiency to gain simplicity. For example, the result of
 * [DocumentSnapshot.toObject] is not cached so the same object may be deserialized
 * many times as the user scrolls.
 */
abstract class FirestoreAdapter<VH : RecyclerView.ViewHolder>(private var mQuery: Query?) : RecyclerView.Adapter<VH>(), EventListener<QuerySnapshot> {
    private var mRegistration: ListenerRegistration? = null

    val snapshots = ArrayList<DocumentSnapshot>()

    override fun onEvent(documentSnapshots: QuerySnapshot?, e: FirebaseFirestoreException?) {
        if (e != null) {
            Log.w(TAG, "onEvent:error", e)
            onError(e)
            return
        }

        // Dispatch the event
        Log.d(TAG, "onEvent:numChanges:" + documentSnapshots!!.documentChanges.size)
        for (change in documentSnapshots.documentChanges) {
            when (change.type) {
                DocumentChange.Type.ADDED -> onDocumentAdded(change)
                DocumentChange.Type.MODIFIED -> onDocumentModified(change)
                DocumentChange.Type.REMOVED -> onDocumentRemoved(change)
            }
        }

        onDataChanged()
    }

    fun startListening() {
        if (mQuery != null && mRegistration == null) {
            mRegistration = mQuery!!.orderBy("type", Query.Direction.DESCENDING).orderBy("title", Query.Direction.ASCENDING).addSnapshotListener(this)
        }
    }

    fun stopListening() {
        if (mRegistration != null) {
            mRegistration!!.remove()
            mRegistration = null
        }

        snapshots.clear()
        notifyDataSetChanged()
    }

    fun setQuery(query: Query) {
        // Stop listening
        stopListening()

        // Clear existing data
        snapshots.clear()
        notifyDataSetChanged()

        // Listen to new query
        mQuery = query
        startListening()
    }

    override fun getItemCount(): Int {
        return snapshots.size
    }

    protected fun getSnapshot(index: Int): DocumentSnapshot {
        return snapshots[index]
    }

    protected fun onDocumentAdded(change: DocumentChange) {
        snapshots.add(change.newIndex, change.document)
        notifyItemInserted(change.newIndex)
    }

    protected fun onDocumentModified(change: DocumentChange) {
        if (change.oldIndex == change.newIndex) {
            // Item changed but remained in same position
            snapshots[change.oldIndex] = change.document
            notifyItemChanged(change.oldIndex)
        } else {
            // Item changed and changed position
            snapshots.removeAt(change.oldIndex)
            snapshots.add(change.newIndex, change.document)
            notifyItemMoved(change.oldIndex, change.newIndex)
        }
    }

    protected fun onDocumentRemoved(change: DocumentChange) {
        snapshots.removeAt(change.oldIndex)
        notifyItemRemoved(change.oldIndex)
    }

    protected open fun onError(e: FirebaseFirestoreException) {}

    protected open fun onDataChanged() {}

    companion object {

        private val TAG = "FirestoreAdapter"
    }
}
