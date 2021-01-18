package com.miquido.restmock.sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.miquido.restmock.sample.api.AddCarRequestDto
import com.miquido.restmock.sample.api.SampleApi
import com.miquido.restmock.sample.api.UpdateCarRequestDto
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import kotlinx.android.synthetic.main.activity_main.addCarButton
import kotlinx.android.synthetic.main.activity_main.addCarErrorButton
import kotlinx.android.synthetic.main.activity_main.getAllCarsButton
import kotlinx.android.synthetic.main.activity_main.getCarButton
import kotlinx.android.synthetic.main.activity_main.getCarId3Button
import kotlinx.android.synthetic.main.activity_main.getCarId99Button
import kotlinx.android.synthetic.main.activity_main.getCarsFordButton
import kotlinx.android.synthetic.main.activity_main.requestNameTextView
import kotlinx.android.synthetic.main.activity_main.responseTextView
import kotlinx.android.synthetic.main.activity_main.statusTextView
import kotlinx.android.synthetic.main.activity_main.updateCarButton
import kotlinx.android.synthetic.main.activity_main.updateCarErrorButton
import kotlinx.android.synthetic.main.activity_main.updateCarId3Button
import org.koin.android.ext.android.inject
import retrofit2.HttpException
import timber.log.Timber

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private val sampleApi: SampleApi by inject()

    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getAllCarsButton.setOnClickListener { getAllCars() }
        getCarsFordButton.setOnClickListener { getFordCars() }
        addCarButton.setOnClickListener { addCar("X3") }
        addCarErrorButton.setOnClickListener { addCar("error") }
        getCarButton.setOnClickListener { getCar(1) }
        getCarId3Button.setOnClickListener { getCar(3) }
        getCarId99Button.setOnClickListener { getCar(99) }
        updateCarButton.setOnClickListener { updateCar(1, "TestModel") }
        updateCarId3Button.setOnClickListener { updateCar(3, "TestModel") }
        updateCarErrorButton.setOnClickListener { updateCar(3, "error") }
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }

    private fun getAllCars() {
        sampleApi.getCars()
            .subscribeOn(SchedulerProvider.io())
            .observeOn(SchedulerProvider.ui())
            .doOnSubscribe {
                setRequestName("Get All Cars")
            }
            .subscribeBy(
                onSuccess = {
                    statusTextView.text = "200"
                    responseTextView.text = it.toString()
                },
                onError = {
                    Timber.e(it)
                    handleException(it)
                }
            )
            .addTo(disposables)
    }

    private fun getFordCars() {
        sampleApi.getCars(make = arrayOf("Ford", "Audi"))
            .subscribeOn(SchedulerProvider.io())
            .observeOn(SchedulerProvider.ui())
            .doOnSubscribe {
                setRequestName("Get Cars (?make=Ford)")
            }
            .subscribeBy(
                onSuccess = {
                    statusTextView.text = "200"
                    responseTextView.text = it.toString()
                },
                onError = {
                    Timber.e(it)
                    handleException(it)
                }
            )
            .addTo(disposables)
    }

    private fun addCar(model: String) {
        val addCarRequestDto = AddCarRequestDto(
            make = "BMW",
            model = model
        )
        sampleApi.addCar(addCarRequestDto)
            .subscribeOn(SchedulerProvider.io())
            .observeOn(SchedulerProvider.ui())
            .doOnSubscribe {
                setRequestName("Add Car (model=$model)")
            }
            .subscribeBy(
                onComplete = {
                    statusTextView.text = "201"
                    responseTextView.text = null
                },
                onError = {
                    Timber.e(it)
                    handleException(it)
                }
            )
            .addTo(disposables)
    }

    private fun getCar(id: Long) {
        sampleApi.getCar(id)
            .subscribeOn(SchedulerProvider.io())
            .observeOn(SchedulerProvider.ui())
            .doOnSubscribe {
                setRequestName("Get Car (id=$id)")
            }
            .subscribeBy(
                onSuccess = {
                    statusTextView.text = "200"
                    responseTextView.text = it.toString()
                },
                onError = {
                    Timber.e(it)
                    handleException(it)
                }
            )
            .addTo(disposables)
    }

    private fun updateCar(id: Long, model: String) {
        val updateCarRequestDto = UpdateCarRequestDto(
            model = model
        )
        sampleApi.updateCar(id, updateCarRequestDto)
            .subscribeOn(SchedulerProvider.io())
            .observeOn(SchedulerProvider.ui())
            .doOnSubscribe {
                setRequestName("Update Car (id=$id, model=$model)")
            }
            .subscribeBy(
                onComplete = {
                    statusTextView.text = "200"
                    responseTextView.text = null
                },
                onError = {
                    Timber.e(it)
                    handleException(it)
                }
            )
            .addTo(disposables)
    }

    private fun handleException(it: Throwable) {
        (it as? HttpException)?.let { httpException ->
            statusTextView.text = httpException.code().toString()
            responseTextView.text = httpException.message()
        }
    }

    private fun setRequestName(requestName: String) {
        requestNameTextView.text = requestName
        statusTextView.text = null
        responseTextView.text = null
    }
}
