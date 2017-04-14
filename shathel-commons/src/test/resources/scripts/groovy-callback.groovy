processor.ansible("APPLY", "./groovy-callback-playbook.yml", env)
assert processor.vagrant("STARTED", "./groovy-callback-vagrant", env)["RESULT"] == "false"
assert processor.vagrant("status default", "./Vagrantfile", env)["OUTPUT"] .startsWith("Current machine states:")
new File(context.settingsDirectory, "groovy_callback_out.txt").text = "hello"