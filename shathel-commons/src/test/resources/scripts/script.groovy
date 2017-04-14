assert env['SAMPLE_ENV']!=null
assert command == 'APPLY'
new File(context.settingsDirectory, "groovy_out.txt").text = "hello"

result.output = "OUTPUT"
result.status = false
env["RETURNED_ENV"] = 'value'