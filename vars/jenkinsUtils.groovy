 def call(body) {
        def config = [:]
        body.resolveStrategy = Closure.DELEGATE_FIRST
        body.delegate = config
        body()
		if ("${config}".contains( 'zipfiles') == true) { zipfiles("${config.zipfiles}") }
 }

 def createMap(directory, data) {
		def fname = sh(returnStdout: true, script: "echo ${data} | cut -f 3 -d '/'")
		echo "Fname is ${fname}"
		def data1 = libraryResource "${data}"
		writeFile file: "/tmp/${directory}/data.txt", text: "${data1}"
		def temp = sh (returnStdout: true, script: "cat /tmp/${directory}/data.txt")
		String out = temp.replaceAll("[\\n]+","|");
	        Map<String, String> map = new HashMap<String, String>();
    		// split on ':' and on '::'
		String[] params = out.split("\\|\\|?");
		for (int i = 0; i < params.length; i += 2) {
	        map.put(params[i], params[i + 1]);
		}
		def val = map.find{it.key == "Africa"}?.value
		if ("${val}" != "null")
			println "val value: ${val}"
		//println val.getClass().name
                //for (String s : map.keySet()) {
                //println(s + map.get(s));
    		//}
 }

 def zipfiles(files) {
	sh "cd ${files}; zip -qr /tmp/prod.zip prod"
 }
