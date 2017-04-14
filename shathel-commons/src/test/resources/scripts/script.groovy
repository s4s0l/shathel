assert env['SAMPLE_ENV']!=null
assert command == 'APPLY'
new File(context.settingsDirectory, "groovy_out.txt").text = "hello"