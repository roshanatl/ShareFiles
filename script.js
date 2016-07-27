$(function()
{
	// Variable to store your files
	var files;
     

     var $jqValue = $('.jqValue');
	 var $tableValue = $('.tableValue');
	// Add events
	$('input[type=file]').on('change', prepareUpload);
	$('form').on('submit', uploadFiles);
    $("#imagefile").change(function ()
              {
                     $("#img").show();
                     $("#img").attr("src",'');
                     if (typeof(FileReader)!="undefined"){

                         var regex = /^([a-zA-Z0-9\s_\\.\-:])+(.jpg|.jpeg|.gif|.png)$/;
                         $($(this)[0].files).each(function () {
                             var getfile = $(this);
                             if (regex.test(getfile[0].name.toLowerCase())) {
                                 var reader = new FileReader();
                                 reader.onload = function (e) {
                                     $("#img").attr("src",e.target.result);
                                 }
                                 reader.readAsDataURL(getfile[0]);
                             } else {
                                 alert(getfile[0].name + " is not image file.");
                                 return false;
                             }
                         });
                     }
                     else {
                         alert("Browser does not supportFileReader.");
                     }
            });

	// Grab the files and set them to our variable
	function prepareUpload(event)
	{
		files = event.target.files;
	}

	// Catch the form submit and upload the files
	function uploadFiles(event)
	{
		event.stopPropagation(); // Stop stuff happening
        event.preventDefault(); // Totally stop stuff happening

        // START A LOADING SPINNER HERE

        // Create a formdata object and add the files
		var data = new FormData();
		var jsondata = {"id":"newmmmmaaaa2","Brand":"Lego","Age":"7-14","Warning":"choking hazard","Raw_Data":"LEGO MARVEL SUPER HEROES Age 7-14 76031THE HULK BUSTER SMASH 248 pcs/pzs WARNING: CHOKING HAZARD TOY CONTAINS SMALL PARTS AND A SMALL BALL.NOT FOR CHILDREN UNDER 3 YEARS.AVENGERSAGE OF ULTRONNEW SUPER JUMPER","Pieces":"248"};
		$.each(files, function(key, value)
		{
			data.append(key, value);
		});
        
        $.ajax({
            url: 'http://www.ocrwebservice.com/restservices/processDocument?language=english&pagerange=1-5&gettext=true',
            type: 'POST',
            data: data,
            cache: false,
			crossDomain : true,
            //dataType: 'json',
            processData: false, // Don't process the files
            contentType: false, // Set content type to false as jQuery will tell the server its a query string request
            success: function(data, textStatus, jqXHR)
            {
            	if(typeof data.error === 'undefined')
            	{
				    $jqValue.html(data);
					var requestBody ={ "id": "newmmmmaaaa2", "text" : data};
					GetTableData(requestBody);
            		// Success so call function to process the form
            		submitForm(event, data);
            	}
            	else
            	{
				 $jqValue.html("Unable to retrieve text");
				 //var tabledata = gettableData(jsondata);
            		// Handle errors here
            		console.log('ERRORS: ' + data.error);
            	}
            },
            error: function(jqXHR, textStatus, errorThrown)
            {
			   $jqValue.html("Unable to retrieve text");
			    //var tabledata = gettableData(jsondata);
            	// Handle errors here
            	console.log('ERRORS: ' + textStatus);
            	// STOP LOADING SPINNER
            }
        });
    }
	
	function GetTableData(requestBody)
	{
	$.ajax({
            url: 'http://localhost:9090/ocr/rest/abzoobaParse/parseText',
            type: 'POST',
            data: requestBody,
            cache: false,
			crossDomain : true,
            //dataType: 'json',
            processData: false, // Don't process the files
            contentType: application/json, // Set content type to false as jQuery will tell the server its a query string request
            success: function(data, textStatus, jqXHR)
            {
            	if(typeof data.error === 'undefined')
            	{
				  var tabledata = gettableData(data);
            	}
            	else
            	{
               	   // Handle errors here
            		console.log('ERRORS: ' + data.error);
            	}
            },
            error: function(jqXHR, textStatus, errorThrown)
            {
            	// Handle errors here
            	console.log('ERRORS: ' + textStatus);
            	// STOP LOADING SPINNER
            }
        });
	}

	function gettableData(jsonObj) {
        var html = '<table border="1">';
        $.each(jsonObj, function(key, value){
            html += '<tr>';
            html += '<td>' + key + '</td>';
            html += '<td>' + value + '</td>';
            html += '</tr>';
        });
        html += '</table>';
        $tableValue.html(html);
    }

    function submitForm(event, data)
	{
		// Create a jQuery object from the form
		$form = $(event.target);
		
		// Serialize the form data
		var formData = $form.serialize();
		
		// You should sterilise the file names
		$.each(data.files, function(key, value)
		{
			formData = formData + '&filenames[]=' + value;
		});

		$.ajax({
			url: 'submit.php',
            type: 'POST',
            data: formData,
            cache: false,
            dataType: 'json',
            success: function(data, textStatus, jqXHR)
            {
            	if(typeof data.error === 'undefined')
            	{
            		// Success so call function to process the form
            		console.log('SUCCESS: ' + data.success);
            	}
            	else
            	{
            		// Handle errors here
            		console.log('ERRORS: ' + data.error);
            	}
            },
            error: function(jqXHR, textStatus, errorThrown)
            {
            	// Handle errors here
            	console.log('ERRORS: ' + textStatus);
            },
            complete: function()
            {
            	// STOP LOADING SPINNER
            }
		});
	}
});