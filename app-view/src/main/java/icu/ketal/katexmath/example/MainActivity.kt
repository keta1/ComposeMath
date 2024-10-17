package icu.ketal.katexmath.example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.graphics.Color
import android.widget.TextView
import icu.ketal.katexmath.MTMathView
import icu.ketal.katexmath.MTMathView.MTMathViewMode
import icu.ketal.katexmath.MTFontManager
import icu.ketal.katexmath.example.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var menufont: MenuItem
    private lateinit var menumode: MenuItem
    private lateinit var menusize: MenuItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        createEquations()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        menufont = menu.findItem(R.id.menufont)
        menumode = menu.findItem(R.id.menumode)
        menusize = menu.findItem(R.id.menusize)
        return true
    }


    private var sampleEquations: MutableList<MTMathView> = mutableListOf()
    private var defaultFontSize = 20f  // Starting fontsize dp pixels

    private fun createEquations() {
        val layoutPadHoriz = 24
        val layoutPadVert = 42

        // Some padding around the equations. Cosmetic
        val layoutParams = LinearLayout.LayoutParams(0, 0)
        layoutParams.setMargins(layoutPadHoriz, layoutPadVert, layoutPadHoriz, layoutPadVert)

        /*
             We read a plain text file with LaTeX strings and comments
             Each LaTeX string is placed in a separate MTMathViews.
             Comments are placed in TextViews.
             All are placed into a view to scroll vertically and horizontally.
         */
        val inputStream = getResources().openRawResource(R.raw.samples)
        val lineList = mutableListOf<String>()

        inputStream.bufferedReader().useLines { lines -> lines.forEach { lineList.add(it) } }

        lineList.forEach {
            if (it.isNotBlank()) {
                if (it[0] == '#') {
                    val tv = TextView(this)
                    tv.text = it.trim()
                    tv.setTextColor(Color.DKGRAY)
                    println("textSize ${tv.textSize}")
                    binding.contentMain.mainLayout.addView(tv)
                } else {
                    val mathView = MTMathView(this)
                    mathView.fontSize = MTMathView.convertDpToPixel(defaultFontSize)
                    mathView.latex = it
                    sampleEquations.add(mathView)
                    binding.contentMain.mainLayout.addView(mathView, layoutParams)
                }
            }
        }
    }


    private fun applyfont(fontname: String) {
        for (eq in sampleEquations) {
            eq.font = MTFontManager.fontWithName(fontname, eq.fontSize)
        }
    }

    private fun applyfontsize(fontsize: Float) {
        val pixelFontSize = MTMathView.convertDpToPixel(fontsize)
        for (eq in sampleEquations) {
            eq.font = MTFontManager.fontWithName(eq.font!!.name, pixelFontSize)
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
        // Font menu
            R.id.fontdefault -> {
                applyfont("latinmodern-math")
                fontmenucheck(R.id.fontdefault)
                return true
            }
            R.id.fonttermes -> {
                applyfont("texgyretermes-math")
                fontmenucheck(R.id.fonttermes)
                return true
            }
            R.id.fontxits -> {
                applyfont("xits-math")
                fontmenucheck(R.id.fontxits)
                return true
            }
        // Size Menu
            R.id.f12 -> {
                applyfontsize(12.0f)
                sizemenucheck(R.id.f12)
                return true
            }
            R.id.f16 -> {
                applyfontsize(16.0f)
                sizemenucheck(R.id.f16)
                return true
            }
            R.id.f20 -> {
                applyfontsize(20f)
                sizemenucheck(R.id.f20)
                return true
            }
            R.id.f40 -> {
                applyfontsize(40f)
                sizemenucheck(R.id.f40)
                return true
            }
        // Mode Menu
            R.id.modedisplay -> {
                for (eq in sampleEquations) {
                    eq.labelMode = MTMathViewMode.KMTMathViewModeDisplay
                }
                modemenucheck(R.id.modedisplay)
                return true
            }
            R.id.modetext -> {
                for (eq in sampleEquations) {
                    eq.labelMode = MTMathViewMode.KMTMathViewModeText
                }
                modemenucheck(R.id.modetext)
                return true
            }
        // Good for profiling
            R.id.modereload -> {
                binding.contentMain.mainLayout.removeAllViewsInLayout()
                sampleEquations.clear()
                binding.contentMain.mainLayout.invalidate()
                // Nice to see the screen show blank before adding back equations
                binding.contentMain.mainLayout.postDelayed({ createEquations() }, 10)
                sizemenucheck(R.id.f20)
                modemenucheck(R.id.modedisplay)
                fontmenucheck(R.id.fontdefault)
                return true
            }


        // Color Menu
            R.id.colorblack -> {
                for (eq in sampleEquations) {
                    eq.textColor = Color.BLACK
                }
                return true
            }
            R.id.colorpurple -> {
                for (eq in sampleEquations) {
                    eq.textColor = Color.MAGENTA
                }
                return true
            }


            else -> super.onOptionsItemSelected(item)
        }
    }

    // Utility functions to make sure menu checked state is correct
    private fun menucheck(menu: MenuItem, thisone: Int) {
        val m = menu.subMenu
        for (i in 0 until m!!.size()) {
            val mi = m.getItem(i)
            if (mi.itemId == thisone) {
                mi.setChecked(true)
            } else {
                mi.setChecked(false)
            }
        }
    }

    private fun fontmenucheck(thisone: Int) {
        menucheck(menufont, thisone)
    }

    private fun modemenucheck(thisone: Int) {
        menucheck(menumode, thisone)
    }

    private fun sizemenucheck(thisone: Int) {
        menucheck(menusize, thisone)
    }
}
