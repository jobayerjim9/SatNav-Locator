package com.jobayerjim9.satnav.ui

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.jobayerjim9.satnav.R
import com.jobayerjim9.satnav.databinding.FragmentChooserDialogBinding
import com.jobayerjim9.satnav.utility.SelectItemListener


class ChooserDialogFragment(val type:String, val listener: SelectItemListener,val names:ArrayList<String>) : DialogFragment(),SelectItemListener {
    lateinit var binding:FragmentChooserDialogBinding
    var backupNames:ArrayList<String> = ArrayList()
    override fun selectedItem(type: String, position: Int) {
        listener.selectedItem(this.type,position)
        dismiss()
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chooser_dialog, null, false)
        initView()
        builder.setView(binding.root)
        return builder.create()
    }

    private fun initView() {
        backupNames.addAll(names)
        binding.chooserRecycler.layoutManager=LinearLayoutManager(requireContext())
        binding.chooserRecycler.adapter=ChooserAdapter(requireContext(),names,this)

        binding.searchTerm.addTextChangedListener(object  : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                val text=s.toString()
                names.clear()
                if (text.isEmpty()) {
                    names.addAll(backupNames)
                    binding.chooserRecycler.adapter=ChooserAdapter(requireContext(),names,this@ChooserDialogFragment)
                }
                else{
                    for (name in backupNames) {
                        if (name.toLowerCase().trim().contains(text.toLowerCase().trim())) {
                            names.add(name)
                        }
                    }
                    binding.chooserRecycler.adapter=ChooserAdapter(requireContext(),names,this@ChooserDialogFragment)

                }
            }

        })
    }


}