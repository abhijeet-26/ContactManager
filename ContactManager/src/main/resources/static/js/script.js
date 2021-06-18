const search = () => {
	let query = $("#search-input").val();



	if (query == '') {
		$(".search-result").hide();
	}
	else {
		//console.log(query);

		// sending request to server

		let url = `http://localhost:7070/search/${query}`;

		fetch(url)
			.then((response) => {
				return response.json();
			})
			.then((data) => {
				console.log(data);
				let text = `<div class='list-group'>`;

				console.log(text);

				data.forEach((contact) => {
					text += `<a href='/user/contact/${contact.cid}' class='list-group-item list-group-item-action'>${contact.name}</a>`;
				})

				text += `</div > `;
				$(".search-result").html(text);
				$(".search-result").show();

			});



	}



}
