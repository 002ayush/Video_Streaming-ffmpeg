import {
	Alert,
	Button,
	Card,
	Progress,
	Textarea,
	TextInput,
} from "flowbite-react";
import playbtn from "../assets/playbtn.png";
import { useRef, useState } from "react";
import {toast} from "react-hot-toast";
import axios from "axios";
function VideoUpload() {
	const [selectedFile, setSelectedFile] = useState(null);
	const [progress, setProgress] = useState(0);
	const [data, setData] = useState({ title: "", description: "" });
	const [uploading, setUploading] = useState(false);
	const fileref = useRef(null);
	const [message, setMessage] = useState("");
	function handleChangeForm(event) {
		// console.log(event.target.value);
		setData({ ...data, [event.target.name]: event.target.value });
		console.log(data);
	}
	function handleForm(event) {
		event.preventDefault();

		if (selectedFile == null) {
			toast.success("You have to upload video file", {
				position: "top-right",
				theme: "dark",
			});
			return;
		}
		saveVideo();
	}
	function handleFileChange(event) {
		console.log(event.target.files[0]);

		setSelectedFile(event.target.files[0]);
	}
	function resetform(){
		setData({
			title:"",
			description:""
		});
		setMessage("");
		setProgress(0);
		setUploading(false);
		setSelectedFile(null);
		fileref.current.value = "";
	}
	async function saveVideo() {
		setUploading(true);
		let formdata = new FormData();
		formdata.append("title", data.title);
		formdata.append("description", data.description);
		formdata.append("file", selectedFile);

		try {
			let response = await axios.post(
				"http://localhost:8080/api/v1/videos",
				formdata,
				{
					headers: {
						"Content-Type": "multipart/form-data",
					},
					onUploadProgress: (progressEvent) => {
						const progress = Math.round(
							(progressEvent.loaded * 100) / progressEvent.total
						);
						setProgress(progress);
					},
				}
			);
			setUploading(false);
			setProgress(0);
			setMessage("Video Uploaded Successfully")
			console.log(response);
			toast.success("File Uploaded Successfully!!!")
			resetform();
		} catch (error) {
			console.error(error);
			setProgress(0);
			setUploading(false);
			setMessage("Video is not uploaded!!!");
			toast.error("File is not uploaded successfully!!!");
			resetform();
		}
	}
	return (
		<>
			<div className="flex justify-center items-center">
				
				<Card className="bg-slate-600">
					<div className="flex flex-col space-y-5  px-20">
						<h1 className="text-2xl">Video Streaming Application</h1>
						<form
							onSubmit={handleForm}
							className="flex flex-col space-y-6 justify-center"
							action=""
						>
							<div className="text-xl">
								<label htmlFor="title">Video Title</label>
								<TextInput
								value={data.title}
									onChange={handleChangeForm}
									name="title"
									id="title"
								/>
							</div>
							<div className="text-xl">
								<label htmlFor="description">Video Description</label>
								<Textarea
								value={data.description}
									onChange={handleChangeForm}
									rows={6}
									name="description"
									id="description"
								/>
							</div>

							<div className="flex my-5">
								<div>
									<img src={playbtn} alt="#" height={50} width={50} />
								</div>
								<div>
									<input
										ref={fileref}
										onChange={handleFileChange}
										className=" text-sm text-gray-900 border border-gray-300 rounded-lg cursor-pointer bg-gray-50 dark:text-gray-400 focus:outline-none dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400"
										id="file_input"
										type="file"
									/>
								</div>
							</div>
							{uploading && (
								<Progress
									progress={progress}
									textLabelPosition="inside"
									textLabel="Uploading"
									size="0.8em"
									color="red"
									labelProgress
									labelText
								/>
							)}
							{message && (
								<Alert
									color="success"
									rounded
									withBorderAccent
									onDismiss={() => setMessage("")}
								>
									<span className="font-medium">
										{message}
									</span>
								</Alert>
							)}
							<div className="mx-auto flex justify-center items-center my-5">
								<Button
									className=" mb-2 text-sm font-medium text-gray-900 dark:text-white"
									htmlFor="file_input"
									type="submit"
								>
									Upload file
								</Button>
							</div>
						</form>
					</div>
				</Card>
			</div>
		</>
	);
}

export default VideoUpload;
