const fs = require('fs');

const manifestPath = process.env.UNI_INPUT_DIR + '/manifest.json';

let Manifest = fs.readFileSync(manifestPath, {
	encoding: 'utf-8'
});

function mpliveMainfestPlugin(isOpen) {
	if (process.env.UNI_PLATFORM !== 'mp-weixin') return;

	const manifestData = JSON.parse(Manifest)

	if (isOpen === '0') {
		delete manifestData['mp-weixin'].plugins['live-player-plugin'];
	}

	if (isOpen === '1') {
		manifestData['mp-weixin'].plugins['live-player-plugin'] = {
			"version": "1.3.5",
			"provider": "wx2b03c6e691cd7370"
		}
	}

	Manifest = JSON.stringify(manifestData, null, 2)

	fs.writeFileSync(manifestPath, Manifest, {
		"flag": "w"
	})
}

export default mpliveMainfestPlugin
