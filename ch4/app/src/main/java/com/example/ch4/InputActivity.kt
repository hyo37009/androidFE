package com.example.ch4

import android.app.DatePickerDialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import com.example.ch4.databinding.ActivityInputBinding
import com.example.ch4.databinding.ActivityMainBinding

class InputActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInputBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input)

        binding = ActivityInputBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bloodtypeSpinner.adapter = ArrayAdapter.createFromResource(
            this,
            R.array.blood_types,
            android.R.layout.simple_list_item_1
        )
        binding.birthdateLayer.setOnClickListener {
            val listener =
                DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                    binding.birthdateValueTextView.text = "$year-${month.inc()}-$dayOfMonth"
                }
            DatePickerDialog(
                this,
                listener,
                2000,
                0,
                1
            ).show()
        }

        binding.warningCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            binding.warningEditText.isVisible = isChecked
        }

        binding.warningEditText.isVisible = binding.warningCheckBox.isChecked

        binding.saveButton.setOnClickListener {
            saveData()
            finish()
        }
    }
    private fun saveData(){
        with(getSharedPreferences(USER_INFORMATION, Context.MODE_PRIVATE).edit()){
            putString(NAME, binding.nameEditText.text.toString())
            putString(BIRTHDATE, binding.birthdateValueTextView.text.toString())
            putString(BLOOD_TYPE, getBloodType())
            putString(EMERGENCY_CONTACT, binding.emergencyContactEditText.text.toString())
            putString(WARNING, getWarning())
            apply()

        }

        Toast.makeText(this, "저장을 완료했습니다.", Toast.LENGTH_SHORT).show()

    }

    private fun getBloodType() : String{
        val bloodAlphabet = binding.bloodtypeSpinner.selectedItem.toString()
        val bloodSign = if(binding.bloodtypePlus.isChecked) "+" else "-"
        return "$bloodSign$bloodAlphabet"
    }

    private fun getWarning() : String{
        return if(binding.warningCheckBox.isChecked) binding.warningEditText.text.toString() else ""
    }


}