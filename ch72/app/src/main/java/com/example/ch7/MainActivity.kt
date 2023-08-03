package com.example.ch7

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ch7.databinding.ActivityMainBinding
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity(), WordAdaptor.ItemClickListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var wordAdaptor: WordAdaptor
    private var selectedWord: Word? = null
    private val updateAddWordResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val isUpdated = result.data?.getBooleanExtra("isUpdated", false) ?: false

        if ((result.resultCode == RESULT_OK) && isUpdated) {
            updateAddWord()
        }

    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val updateEditWordResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val editWord = result.data?.getParcelableExtra<Word>("editWord")
        if ((result.resultCode == RESULT_OK) && editWord != null) {
            updateEditWord(editWord)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initRecyclerView()
        binding.addButton.setOnClickListener {
            Intent(this, AddActivity::class.java).let {
                updateAddWordResult.launch(it)
            }
        }

        binding.deleteImageView.setOnClickListener {
            delete()
        }

        binding.editImageView.setOnClickListener {
            edit()
        }
    }

    @SuppressLint("NewApi")
    private fun edit() {
        if (selectedWord == null) return


        val intent = Intent(this, AddActivity::class.java).putExtra("originWord", selectedWord)
        updateEditWordResult.launch(intent)
    }


    private fun initRecyclerView() {
        wordAdaptor = WordAdaptor(mutableListOf(), this)
        binding.wordRecyclerView.apply {
            // adapter랑 layoutManager를 까먹지 말자
            adapter = wordAdaptor
            layoutManager =
                LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)

            // divider를 넣기 (UI자체에 넣을 때도 있음)
            val dividerItemDecoration =
                DividerItemDecoration(applicationContext, LinearLayoutManager.VERTICAL)
            addItemDecoration(dividerItemDecoration)
        }

        Thread {
            val list = AppDatabase.getInstance(this)?.wordDao()?.getAll() ?: emptyList()
            wordAdaptor.list.addAll(list)
            runOnUiThread { wordAdaptor.notifyDataSetChanged() }
        }.start()

    }

    private fun updateAddWord() {
        thread {
            AppDatabase.getInstance(this)?.wordDao()?.getLatestWord()?.let { word ->
                wordAdaptor.list.add(0, word)
                runOnUiThread { wordAdaptor.notifyDataSetChanged() }
            }

        }.start()
    }

    private fun updateEditWord(word: Word) {
        val index = wordAdaptor.list.indexOfFirst { it.id == word.id }
        wordAdaptor.list[index] = word
        runOnUiThread {
            selectedWord = word
            wordAdaptor.notifyItemChanged(index)
            binding.textTextView.text = word.text
            binding.meanTextView.text = word.mean
        }
    }

    private fun delete() {
        if (selectedWord == null) return

        Thread {
            selectedWord?.let { word ->
                AppDatabase.getInstance(this)?.wordDao()?.delete(word)
                runOnUiThread {
                    wordAdaptor.list.remove(word)
                    wordAdaptor.notifyDataSetChanged()
                    binding.textTextView.text = ""
                    binding.meanTextView.text = ""
                    Toast.makeText(this, "${word.text}가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    override fun onClick(word: Word) {
        selectedWord = word
        binding.textTextView.text = word.text
        binding.meanTextView.text = word.mean
    }
}