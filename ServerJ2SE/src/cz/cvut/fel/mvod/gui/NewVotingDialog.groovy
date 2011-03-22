/*
 * © 2010, Jakub Valenta
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the Jakub Valenta
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * This software is provided by the copyright holders and contributors “as is” and any
 * express or implied warranties, including, but not limited to, the implied
 * warranties of merchantability and fitness for a particular purpose are disclaimed.
 * In no event shall the foundation or contributors be liable for any direct, indirect,
 * incidental, special, exemplary, or consequential damages (including, but not limited to,
 * procurement of substitute goods or services; loss of use, data, or profits; or business
 * interruption) however caused and on any theory of liability, whether in contract, strict
 * liability, or tort (including negligence or otherwise) arising in any way out of the use
 * of this software, even if advised of the possibility of such damage.
 */

package cz.cvut.fel.mvod.gui
import java.awt.FlowLayout
import cz.cvut.fel.mvod.common.Voting
import java.awt.BorderLayout
import java.awt.GridLayout
import javax.swing.DefaultComboBoxModel
import groovy.swing.SwingBuilder
import javax.swing.JFrame
import javax.swing.WindowConstants
import cz.cvut.fel.mvod.common.EvaluationType
import cz.cvut.fel.mvod.persistence.DAOFacadeImpl

/**
 * Dialog pro vytvoření nového hlasování.
 * @author jakub
 */
class NewVotingDialog implements Showable {

	/**
	 * SwingBuilder pro vytvoření dialogu.
	 */
	def builder
	/**
	 * Nadřazené okno (JFrame).
	 */
	def owner
	/**
	 * JComboBox pro výběr typu hlasování.
	 */
	def votingType
	/**
	 * JPanel zobrazující nastavení hlasování.
	 */
	def votingSettingsPanel
	/**
	 * JComboBox pro výběr typu hlasování (volby).
	 */
	def votingSettings
	/**
	 * JSpinner zobrazující hranici platnosti volby.
	 */
	def votingMinParticipation
	/**
	 * JPanel zobrazující nastavení testu.
	 */
	def testSettingsPanel
	/**
	 * JComboBox pro výběr typu vyhodnocení testu.
	 */
	def testSettings
	/**
	 * JTextArea zobrazující nápovědu.
	 */
	def helpArea
	def final TYPES = ["Hlasování", "Test"]
	def final VOTING_SETTINGS = ["Veřejné", "Tajné"]
	def final TEST_SETTINGS = ["Částečné", "Absolutní", "S odčítáním"]
	def final HELP = ["Za částečně správnou odpověď je přidělena část bodů.",
				"Body jsou přidělené pouze za správně zodpovězené otázky.",
				"Body se přičítají za správně zodpovězené otázky a odečítají za špatně zodpovězené."]


	NewVotingDialog(SwingBuilder builder, JFrame owner) {
		this.builder = builder
		this.owner = owner
	}

	void show() {
		votingType.selectedItem = TYPES[0]
		votingSettings.selectedItem = VOTING_SETTINGS[0]
		testSettings.selectedItem = TEST_SETTINGS[0]
		votingTypeSelect()
		testSettingsSelect()
		newVotingDialog.pack()
		newVotingDialog.visible = true
	}

	void hide() {
		newVotingDialog.visible = false
	}

	/**
	 * Vytvoří a uloží nové hlasování.
	 */
	def createAction = {
		def voting = new Voting()
		voting.test = votingType.selectedItem == TYPES[1]
		if(!voting.test) {
			voting.secret = votingSettings.selectedItem == VOTING_SETTINGS[1]
			voting.minVoters = (Integer) votingMinParticipation.value
		} else {
			switch(testSettings.selectedItem) {
				case TEST_SETTINGS[0]:
					voting.evaluation = EvaluationType.PARTIAL_CORRECTNESS
					break;
				case TEST_SETTINGS[1]:
					voting.evaluation = EvaluationType.ABSOLUTE_CORRECTNESS
					break;
				case TEST_SETTINGS[2]:
					voting.evaluation = EvaluationType.ABSOLUTE_CORRECTNESS_NEGATIVE
					break;
			}
		}
		DAOFacadeImpl.instance.currentVoting = voting
		hide()
	}

	/**
	 * Překreslí dialog po změně typu hlasování.
	 */
	def votingTypeSelect =  {
		votingSettingsPanel.visible = votingType.selectedItem == TYPES[0]
		testSettingsPanel.visible = votingType.selectedItem == TYPES[1]
		newVotingDialog.pack()
	}

	/**
	 * Překreslí dialog při změně typu vyhodnocení testu.
	 */
	def testSettingsSelect = {
		helpArea.text = HELP[testSettings.selectedIndex]
	}

	/**
	 * Dialog pro vytvoření nového hlasování.
	 */
	def newVotingDialog = builder.dialog(
			title: "Nové hlasování",
			maximumSize: [200, 200],
			resizable: false,
			owner: owner,
			layout: new BorderLayout(),
			modal: true,
			locationRelativeTo: owner,
			defaultCloseOperation: WindowConstants.DISPOSE_ON_CLOSE) {
		panel(constraints: BorderLayout.NORTH,
				layout: new GridLayout(1, 2)) {
			label(text: "Typ události:")
			votingType = comboBox(
				model: new DefaultComboBoxModel(TYPES.toArray()),
				selectedIndex: 0, actionPerformed: votingTypeSelect)
		}
		panel(constraints: BorderLayout.CENTER) {
			votingSettingsPanel = panel(layout: new GridLayout(2, 2)) {
				label(text: "Typ hlasování:")
				votingSettings = comboBox(
						model: new DefaultComboBoxModel(VOTING_SETTINGS.toArray()),
						selectedIndex: 0)
				label(text: "Hranice platnosti v %:")
				votingMinParticipation = spinner(
						model: spinnerNumberModel(maximum: 100, minimum: 0, stepSize: 1))
			}
			testSettingsPanel = panel(
					visible: false,
					constraints: BorderLayout.CENTER,
					layout: new BorderLayout()) {
				panel(
						layout: new GridLayout(1, 2),
						constraints: BorderLayout.NORTH) {
					label(text: "Způsob vyhodnocení:")
					testSettings = comboBox(
						model: new DefaultComboBoxModel(TEST_SETTINGS.toArray()),
						selectedIndex: 0, actionPerformed: testSettingsSelect)
				}
				helpArea = textArea(
						editable: false,
						text: HELP[0],
						rows: 3,
						lineWrap: true,
						wrapStyleWord:true,
						constraints: BorderLayout.CENTER)
			}
		}
		panel(constraints: BorderLayout.SOUTH, layout: new FlowLayout(FlowLayout.RIGHT)) {
			button(text: "Uložit", actionPerformed: createAction)
			button(text: "Zrušit", actionPerformed: {hide()})
		}
	}

}
