package net.xblacky.animexstream.ui.main.settings

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.android.synthetic.main.fragment_settings.view.*
import net.xblacky.animexstream.MainActivity
import net.xblacky.animexstream.R

class SettingsFragment: Fragment(), View.OnClickListener {

    private lateinit var rootView: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_settings, container, false)
        setupClickListeners()
        setupToggleText()
        return rootView
    }

    private fun setupToggleText() {
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_NO) {
            rootView.toggleMode.text = getString(R.string.toggle_to_night_mode)
        } else {
            rootView.toggleMode.text = getString(R.string.toggle_to_light_mode)
        }
    }

    private fun setupClickListeners() {
        rootView.back.setOnClickListener(this)
        rootView.toggleMode.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.back -> findNavController().popBackStack()
            R.id.toggleMode -> setupToggle()
        }
    }

    private fun setupToggle() {
        rootView.toggleMode.setOnClickListener {
            if ((activity as MainActivity).resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
//                rootView.toggleMode.text = getString(R.string.toggle_to_night_mode)
                Toast.makeText(context, "Light Mode", Toast.LENGTH_SHORT).show()
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
//                rootView.toggleMode.text = getString(R.string.toggle_to_light_mode)
                Toast.makeText(context, "Night Mode", Toast.LENGTH_SHORT).show()
            }
        }
    }
}