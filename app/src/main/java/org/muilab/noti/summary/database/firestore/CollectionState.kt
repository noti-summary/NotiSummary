package org.muilab.noti.summary.database.firestore

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.State
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

/**
 * A value holder where reads to the [value] property during the execution of a [Composable]
 * function, the current [RecomposeScope] will be subscribed to changes of that value. When the
 * [value] property is changed in that Firestore Collection, a recomposition of any subscribed
 * [RecomposeScope]s will be scheduled.
 *
 * @see [State]
 * @see [collectionStateOf]
 */
interface CollectionState : State<FirestoreCollection>, LifecycleObserver {
    override val value: FirestoreCollection
    fun startListening()
    fun stopListening()

    operator fun component1(): FirestoreCollection
}

/**
 * Return a new [CollectionState] initialized with the passed [query]
 *
 * The CollectionState class is a single value holder whose reads are observed by
 * Compose.
 *
 * @param query the collection query to be observed
 * @param lifecycleOwner the lifecycle owner that the state should react to
 *
 * @see State
 * @see CollectionState
 */
fun collectionStateOf(
    query: Query,
    lifecycleOwner: LifecycleOwner? = null
) = object : CollectionState {
    private var listener: ListenerRegistration? = null
    private var valueState: FirestoreCollection by mutableStateOf(FirestoreCollection.Loading)

    override val value: FirestoreCollection
        get() = valueState

    init {
        lifecycleOwner?.lifecycle?.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    override fun startListening() {
        if (listener == null) {
            listener = query.addSnapshotListener { value, error ->
                value?.let { querySnapshot ->
                    valueState = FirestoreCollection.Snapshot(querySnapshot.documents)
                }
                error?.let { exception ->
                    valueState = FirestoreCollection.Error(exception)
                }
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    override fun stopListening() {
        listener?.remove()
    }

    override fun component1(): FirestoreCollection = value
}