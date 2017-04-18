processor.run("ansible", "APPLY", "./groovy-callback-playbook.yml", env)
assert processor.run("vagrant", "STARTED", "./groovy-callback-vagrant", env).status == false
assert processor.run("vagrant", "status default", "./Vagrantfile", env).output.startsWith("Current machine states:")
assert processor.run("terraform", "plan -no-color", "./tf", env).output.endsWith("Plan: 1 to add, 0 to change, 0 to destroy.")
assert processor.run("packer", "APPLY", "./groovy-callback-packer.json", env).output.contains("Build 'docker' finished.")
new File(context.settingsDirectory, "groovy_callback_out.txt").text = "hello"