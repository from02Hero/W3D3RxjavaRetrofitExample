package com.example.w3d3_rxjavaretrofitexample.view

import android.content.res.Resources
import android.os.Bundle
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.w3d3_rxjavaretrofitexample.R
import com.example.w3d3_rxjavaretrofitexample.network.ApiClient.client
import com.example.w3d3_rxjavaretrofitexample.network.ApiService
import com.example.w3d3_rxjavaretrofitexample.network.model.DataModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observables.ConnectableObservable
import io.reactivex.schedulers.Schedulers
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private val from = "DEL"
    private val to = "HYD"

    // CompositeDisposable is used to dispose the subscriptions in onDestroy() method.
    private val disposable = CompositeDisposable()

    lateinit var apiService: ApiService
    lateinit var mAdapter: DataAdapter
    private var dataList: MutableList<DataModel> = mutableListOf()

    lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        apiService = client!!.create(ApiService::class.java)

        initRecyclerView()

        /***
         * You can notice replay() operator (getTickets(from, to).replay()) is used to make an
         * Observable emits the data on new subscriptions without re-executing the logic again.
         * In our case, the list of tickets will be emitted without making the HTTP call again.
         * Without the replay method, you can notice the fetch tickets HTTP call get executed
         * multiple times.
         */
        val dataObservable: ConnectableObservable<MutableList<DataModel>> =
            getData().replay()

        /**
         * Fetching all tickets first
         * Observable emits List<Ticket> at once
         * All the items will be added to RecyclerView.
         * In the first subscription, the list of tickets directly added to Adapter class and the
         * RecyclerView is rendered directly without price and number of seats.
        </Ticket> */
        disposable.add(
            dataObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { dataList ->
                    mAdapter.update(dataList)
                }.subscribe()
        )
        // Calling connect to start emission
        dataObservable.connect()
    }

    private fun initRecyclerView() {
        mAdapter = DataAdapter(dataList, applicationContext)

        val mLayoutManager: RecyclerView.LayoutManager = GridLayoutManager(this, 1)
        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = mLayoutManager
        recyclerView.addItemDecoration(GridSpacingItemDecoration(1, dpToPx(5), true))
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = mAdapter
    }

    /**
     * Making Retrofit call to fetch all tickets
     */
    private fun getData(): Observable<MutableList<DataModel>> {
        return apiService.photos()
            .toObservable()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }

    private fun dpToPx(dp: Int): Int {
        val r: Resources = resources
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            r.displayMetrics
        ).roundToInt()
    }
}