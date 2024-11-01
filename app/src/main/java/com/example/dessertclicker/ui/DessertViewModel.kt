package com.example.dessertclicker.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.example.dessertclicker.R
import com.example.dessertclicker.model.Dessert
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DessertViewModel(desserts: List<Dessert>) : ViewModel() {
    // Initializing the state with default values or from the first dessert
    private val _uiState = MutableStateFlow(
        DessertUiState(
            currentDessertImageId = desserts.first().imageId,
            currentDessertPrice = desserts.first().price
        )
    )
    val uiState: StateFlow<DessertUiState> = _uiState.asStateFlow()


    //Determine which dessert to show.
    private fun determineDessertToShow(
        desserts: List<Dessert>,
        dessertsSold: Int,
    ): Dessert {
        var dessertToShow = desserts.first()
        for (dessert in desserts) {
            if (dessertsSold >= dessert.startProductionAmount) dessertToShow = dessert
            else break
        }

        return dessertToShow
    }

    fun onDessertClicked(desserts: List<Dessert>) {
        _uiState.update { currentState ->
            val revenue = currentState.revenue.plus(currentState.currentDessertPrice)
            val dessertsSold = currentState.dessertsSold.inc()
            val dessertToShow = determineDessertToShow(desserts, dessertsSold)

            currentState.copy(
                revenue = revenue,
                dessertsSold = dessertsSold,
                currentDessertImageId = dessertToShow.imageId,
                currentDessertPrice = dessertToShow.price
            )
        }
    }


    //Share desserts sold information using ACTION_SEND intent
    private fun shareSoldDessertsInformation(
        intentContext: Context,
        dessertsSold: Int,
        revenue: Int,
    ) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(
                Intent.EXTRA_TEXT,
                intentContext.getString(R.string.share_text, dessertsSold, revenue)
            )
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)

        try {
            ContextCompat.startActivities(intentContext, arrayOf(shareIntent), null)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(
                intentContext,
                intentContext.getString(R.string.sharing_not_available),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun shareSoldDesserts(intentContext: Context) {
        _uiState.value.run {
            shareSoldDessertsInformation(intentContext, dessertsSold, revenue)
        }
    }
}